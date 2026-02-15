package sk.tany.rest.api.service.payment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.LZMAOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.zip.CRC32;

@Service
@Slf4j
public class PayBySquareService {

    private static final int DICT_SIZE_KB = 128;
    private static final int DICT_SIZE_B = DICT_SIZE_KB * 1024;
    private static final int LC = 3;
    private static final int LP = 0;
    private static final int PB = 2;

    public String generateQrCode(BigDecimal amount, String currency, String vs, String ss, String ks, String reference, String note, String iban, String bic) {
        try {
            String serialized = serialize(amount, currency, vs, ss, ks, reference, note, iban, bic);
            String encoded = compressAndEncode(serialized);
            return generateQrImage(encoded);
        } catch (Exception e) {
            log.error("Error generating Pay By Square QR code", e);
            return null;
        }
    }

    // Exposed for testing
    public String generateQrString(BigDecimal amount, String currency, String vs, String ss, String ks, String reference, String note, String iban, String bic) throws IOException {
        String serialized = serialize(amount, currency, vs, ss, ks, reference, note, iban, bic);
        return compressAndEncode(serialized);
    }

    private String serialize(BigDecimal amount, String currency, String vs, String ss, String ks, String reference, String note, String iban, String bic) {
        StringBuilder sb = new StringBuilder();
        sb.append("\t"); // 0: Prefix
        sb.append("1"); // 1: Payment count (1)
        sb.append("\t");
        sb.append("1"); // 2: Payment type (1 - normal)
        sb.append("\t");
        sb.append(amount != null ? amount.toString() : ""); // 3: Amount
        sb.append("\t");
        sb.append(currency != null ? currency : ""); // 4: Currency
        sb.append("\t");
        sb.append(""); // 5: Due Date (empty)
        sb.append("\t");
        sb.append(vs != null ? vs : ""); // 6: VS
        sb.append("\t");
        sb.append(ks != null ? ks : ""); // 7: KS
        sb.append("\t");
        sb.append(ss != null ? ss : ""); // 8: SS
        sb.append("\t");
        sb.append(reference != null ? reference : ""); // 9: Reference
        sb.append("\t");
        sb.append(note != null ? note : ""); // 10: Note
        sb.append("\t");
        sb.append("1"); // 11: Bank accounts count (1)
        sb.append("\t");
        sb.append(iban != null ? iban : ""); // 12: IBAN
        sb.append("\t");
        sb.append(bic != null ? bic : ""); // 13: BIC
        sb.append("\t");
        sb.append("0"); // 14: Standing orders count (0)
        sb.append("\t");
        sb.append("0"); // 15: Direct debits count (0)

        return sb.toString();
    }

    private String compressAndEncode(String data) throws IOException {
        byte[] payBytes = data.getBytes(StandardCharsets.UTF_8);

        // CRC32
        CRC32 crc32 = new CRC32();
        crc32.update(payBytes);
        int checksum = (int) crc32.getValue();
        byte[] checksumBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(checksum).array();

        // Prepare uncompressed bytes: Checksum + Data
        byte[] uncompBytes = new byte[checksumBytes.length + payBytes.length];
        System.arraycopy(checksumBytes, 0, uncompBytes, 0, checksumBytes.length);
        System.arraycopy(payBytes, 0, uncompBytes, checksumBytes.length, payBytes.length);

        // LZMA Compression
        LZMA2Options options = new LZMA2Options();
        options.setDictSize(DICT_SIZE_B);
        options.setLcLp(LC, LP);
        options.setPb(PB);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (LZMAOutputStream lzmaOut = new LZMAOutputStream(out, options, false)) {
            lzmaOut.write(uncompBytes);
        } // lzmaOut.finish() is called on close

        byte[] compressedData = out.toByteArray();

        // LZMA Header (2 bytes length of uncompressed data)
        byte[] lzmaHeader = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) uncompBytes.length).array();

        // Combine: 00 00 + Header + Compressed Data
        byte[] prefix = new byte[]{0x00, 0x00};

        ByteArrayOutputStream finalOut = new ByteArrayOutputStream();
        finalOut.write(prefix);
        finalOut.write(lzmaHeader);
        finalOut.write(compressedData);

        byte[] toEncode = finalOut.toByteArray();

        // Base32Hex Encoding
        Base32 base32 = new Base32(true); // true = useHex
        return base32.encodeToString(toEncode).replace("=", "");
    }

    private String generateQrImage(String data) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = Map.of(EncodeHintType.MARGIN, 4);
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 300, 300, hints);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] pngData = pngOutputStream.toByteArray();

        return Base64.getEncoder().encodeToString(pngData);
    }
}
