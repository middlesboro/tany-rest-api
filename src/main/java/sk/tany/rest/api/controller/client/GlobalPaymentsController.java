package sk.tany.rest.api.controller.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.domain.payment.GlobalPaymentsPayment;
import sk.tany.rest.api.domain.payment.GlobalPaymentsPaymentRepository;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.service.common.GlobalPaymentsSigner;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

@Controller
@RequestMapping("/api/payments/global-payments")
@RequiredArgsConstructor
@Slf4j
public class GlobalPaymentsController {

    private final OrderRepository orderRepository;
    private final GlobalPaymentsPaymentRepository globalPaymentsPaymentRepository;
    private final GlobalPaymentsSigner signer;

    @Value("${gpwebpay.merchant-number}")
    private String merchantNumber;

    @Value("${gpwebpay.private-key}")
    private String privateKey;

    @Value("${gpwebpay.private-key-password}")
    private String privateKeyPassword;

    @Value("${gpwebpay.public-key}")
    private String publicKey;

    @Value("${gpwebpay.url}")
    private String paymentUrl;

    @Value("${gpwebpay.return-url}")
    private String returnUrl;

    @Value("${eshop.frontend-url}")
    private String frontendUrl;

    @GetMapping(value = "/redirect/{orderId}", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String redirect(@PathVariable String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        String operation = "CREATE_ORDER";
        String orderNumber = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String amount = String.valueOf(order.getFinalPrice().multiply(BigDecimal.valueOf(100)).intValue());
        String currency = "978"; // EUR
        String depositFlag = "1";
        String merOrderNum = orderId;
        String md = orderId;
        String description = "Order " + order.getOrderIdentifier(); // Simplified description

        // Parameters to sign: MERCHANTNUMBER|OPERATION|ORDERNUMBER|AMOUNT|CURRENCY|DEPOSITFLAG|MERORDERNUM|URL|DESCRIPTION|MD|USERPARAM1|ADDINFO
        // Note: USERPARAM1 and ADDINFO are optional and not used here, but must be in signature if used.
        // Based on PHP module:
        // $source_for_sign = $merchant_number . "|" . $operation . "|" . $timestamp . "|" . $total_converted . "|" . $currency_iso . "|" . $deposit_flag . "|" . $order_id . "|" . $return_url . "|" . $description . "|" . $md . "|" . $addInfo;

        // We will assume empty addInfo for now as per minimal implementation.
        // Actually, let's stick to the PHP implementation structure strictly.
        // PHP: $order_id is MERORDERNUM. $timestamp is ORDERNUMBER.

        String textToSign = merchantNumber + "|" + operation + "|" + orderNumber + "|" + amount + "|" + currency + "|" + depositFlag + "|" + merOrderNum + "|" + returnUrl + "|" + description + "|" + md + "|"; // addInfo is empty so trailing pipe + empty string effectively

        // Wait, PHP implementation:
        // $source_for_sign = ... . "|" . $addInfo;
        // if addInfo is empty string, it is just a pipe at the end if strict.
        // But `removeCrLfTabTrim` is called on addInfo. If it results in empty string, it is appended.

        // Let's verify if USERPARAM1 is in PHP signature.
        // PHP:
        // $source_for_sign = $merchant_number . "|" .
        //                    $operation . "|" .
        //                    $timestamp . "|" .
        //                    $total_converted . "|" .
        //                    $currency_iso . "|" .
        //                    $deposit_flag . "|" .
        //                    $order_id . "|" .
        //                    $return_url . "|" .
        //                    $description . "|" .
        //                    $md . "|" .
        //                    $addInfo;

        // It DOES NOT include USERPARAM1 in the signature source in the PHP file I read!
        // But wait, the documentation usually says strictly defined order.
        // The PHP file `payment.php` clearly shows the concatenation order.

        String digest = signer.sign(textToSign, privateKey, privateKeyPassword);

        // Save initial payment attempt
        GlobalPaymentsPayment payment = new GlobalPaymentsPayment();
        payment.setOrderId(orderId);
        payment.setMerchantOrderNumber(orderNumber);
        payment.setStatus("CREATED");
        globalPaymentsPaymentRepository.save(payment);

        StringBuilder form = new StringBuilder();
        form.append("<html><body onload='document.forms[0].submit()'>");
        form.append("<form action='").append(paymentUrl).append("' method='POST'>");
        form.append("<input type='hidden' name='MERCHANTNUMBER' value='").append(merchantNumber).append("' />");
        form.append("<input type='hidden' name='OPERATION' value='").append(operation).append("' />");
        form.append("<input type='hidden' name='ORDERNUMBER' value='").append(orderNumber).append("' />");
        form.append("<input type='hidden' name='AMOUNT' value='").append(amount).append("' />");
        form.append("<input type='hidden' name='CURRENCY' value='").append(currency).append("' />");
        form.append("<input type='hidden' name='DEPOSITFLAG' value='").append(depositFlag).append("' />");
        form.append("<input type='hidden' name='MERORDERNUM' value='").append(merOrderNum).append("' />");
        form.append("<input type='hidden' name='URL' value='").append(returnUrl).append("' />");
        form.append("<input type='hidden' name='DESCRIPTION' value='").append(description).append("' />");
        form.append("<input type='hidden' name='MD' value='").append(md).append("' />");
        form.append("<input type='hidden' name='DIGEST' value='").append(digest).append("' />");
        // ADDINFO is optional and empty here, so we can omit or send empty.
        // PHP adds it to data array even if empty/generated.
        form.append("<input type='hidden' name='ADDINFO' value='' />");
        form.append("</form></body></html>");

        return form.toString();
    }

    @GetMapping("/callback")
    public void callback(
            @RequestParam(name = "OPERATION") String operation,
            @RequestParam(name = "ORDERNUMBER") String orderNumber,
            @RequestParam(name = "MERORDERNUM", required = false) String merOrderNum,
            @RequestParam(name = "MD", required = false) String md,
            @RequestParam(name = "PRCODE") String prCode,
            @RequestParam(name = "SRCODE") String srCode,
            @RequestParam(name = "RESULTTEXT", required = false) String resultText,
            @RequestParam(name = "DIGEST") String digest,
            @RequestParam(name = "DIGEST1") String digest1,
            HttpServletResponse response
    ) throws IOException {

        // Verify signature
        // PHP: $text = $var_operation.'|'.$var_ordernumber;
        // if (isset($_GET['MERORDERNUM']) && !empty($_GET['MERORDERNUM'])) $text .= '|'.$var_merordernum;
        // if (isset($_GET['MD']) && !empty($_GET['MD'])) $text .= '|'.$var_md;
        // $text .= '|'.$var_prcode.'|'.$var_srcode;
        // if (isset($_GET['RESULTTEXT']) && !empty($_GET['RESULTTEXT'])) $text .= '|'.$var_resulttext;

        StringBuilder textToVerify = new StringBuilder();
        textToVerify.append(operation).append("|").append(orderNumber);
        if (merOrderNum != null && !merOrderNum.isEmpty()) {
            textToVerify.append("|").append(merOrderNum);
        }
        if (md != null && !md.isEmpty()) {
            textToVerify.append("|").append(md);
        }
        textToVerify.append("|").append(prCode).append("|").append(srCode);
        if (resultText != null && !resultText.isEmpty()) {
            textToVerify.append("|").append(resultText);
        }

        boolean isValid = signer.verify(textToVerify.toString(), digest, publicKey);

        if (isValid) {
            String orderId = merOrderNum; // We used orderId as MERORDERNUM
            Optional<GlobalPaymentsPayment> paymentOpt = globalPaymentsPaymentRepository.findTopByOrderIdOrderByCreateDateDesc(orderId);

            if (paymentOpt.isPresent()) {
                GlobalPaymentsPayment payment = paymentOpt.get();
                payment.setPrCode(prCode);
                payment.setSrCode(srCode);
                payment.setResultText(resultText);
                payment.setDigest(digest);
                payment.setDigest1(digest1);

                if ("0".equals(prCode) && "0".equals(srCode)) {
                     payment.setStatus("PAYED");
                     orderRepository.findById(orderId).ifPresent(order -> {
                         order.setStatus(OrderStatus.PAYED);
                         orderRepository.save(order);
                     });
                     response.sendRedirect(frontendUrl + "/order/thank-you?orderId=" + orderId);
                } else {
                    payment.setStatus("FAILED");
                    response.sendRedirect(frontendUrl + "/order/error?orderId=" + orderId);
                }
                globalPaymentsPaymentRepository.save(payment);
                return;
            }
        }

        // If invalid or not found
        response.sendRedirect(frontendUrl + "/order/error");
    }
}
