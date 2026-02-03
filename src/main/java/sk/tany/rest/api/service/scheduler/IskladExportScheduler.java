//package sk.tany.rest.api.service.scheduler;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import sk.tany.rest.api.config.ISkladProperties;
//import sk.tany.rest.api.domain.order.Order;
//import sk.tany.rest.api.domain.order.OrderRepository;
//import sk.tany.rest.api.domain.order.OrderStatus;
//import sk.tany.rest.api.service.admin.OrderAdminService;
//
//import java.util.List;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class IskladExportScheduler {
//
//    private final OrderRepository orderRepository;
//    private final OrderAdminService orderAdminService;
//    private final ISkladProperties iskladProperties;
//
//    @Scheduled(cron = "0 */5 * * * *")
//    public void exportOrdersToIsklad() {
//        if (!iskladProperties.isEnabled()) {
//            return;
//        }
//
//        try {
//            List<Order> orders = orderRepository.findAllByIskladImportDateIsNullAndStatusNot(OrderStatus.CANCELED);
//            if (!orders.isEmpty()) {
//                log.info("Found {} orders to export to iSklad", orders.size());
//                for (Order order : orders) {
//                    try {
//                        orderAdminService.exportToIsklad(order.getId());
//                    } catch (Exception e) {
//                        log.error("Failed to export order {} to iSklad", order.getOrderIdentifier(), e);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.error("Error running iSklad export job", e);
//        }
//    }
//}
