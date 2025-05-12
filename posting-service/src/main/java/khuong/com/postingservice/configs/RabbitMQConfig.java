// package khuong.com.postingservice.configs;
// import org.springframework.amqp.core.Binding;
// import org.springframework.amqp.core.BindingBuilder;
// import org.springframework.amqp.core.DirectExchange;
// import org.springframework.amqp.core.Queue;
// import org.springframework.amqp.rabbit.connection.ConnectionFactory;
// import org.springframework.amqp.rabbit.core.RabbitTemplate;
// import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
// import org.springframework.amqp.support.converter.MessageConverter;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;

// @Configuration
// public class RabbitMQConfig {
//     public static final String ORDERS_EXCHANGE = "restaurant.exchange";
//     public static final String NEW_ORDER_ROUTING_KEY = "order.new";
//     public static final String ORDER_STATUS_UPDATE_ROUTING_KEY = "order.status.update";
//     public static final String MENU_AVAILABILITY_ROUTING_KEY = "menu.availability.update";

//     @Value("${kitchen.queue.orders}")
//     private String ordersQueue;

//     @Value("${kitchen.queue.order-updates}")
//     private String orderUpdatesQueue;

//     @Value("${kitchen.queue.menu-updates}")
//     private String menuUpdatesQueue;

//     @Bean
//     DirectExchange ordersExchange() {
//         return new DirectExchange(ORDERS_EXCHANGE);
//     }

//     @Bean
//     Queue ordersQueue() {
//         return new Queue(ordersQueue, true);
//     }

//     @Bean
//     Queue orderUpdatesQueue() {
//         return new Queue(orderUpdatesQueue, true);
//     }

//     @Bean
//     Queue menuUpdatesQueue() {
//         return new Queue(menuUpdatesQueue, true);
//     }

//     @Bean
//     Binding newOrderBinding() {
//         return BindingBuilder.bind(ordersQueue())
//                 .to(ordersExchange())
//                 .with(NEW_ORDER_ROUTING_KEY);
//     }

//     @Bean
//     Binding orderStatusBinding() {
//         return BindingBuilder.bind(orderUpdatesQueue())
//                 .to(ordersExchange())
//                 .with(ORDER_STATUS_UPDATE_ROUTING_KEY);
//     }

//     @Bean
//     Binding menuAvailabilityBinding() {
//         return BindingBuilder.bind(menuUpdatesQueue())
//                 .to(ordersExchange())
//                 .with(MENU_AVAILABILITY_ROUTING_KEY);
//     }

//     @Bean
//     public MessageConverter jsonMessageConverter() {
//         return new Jackson2JsonMessageConverter();
//     }

//     @Bean
//     public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
//         RabbitTemplate template = new RabbitTemplate(connectionFactory);
//         template.setMessageConverter(jsonMessageConverter());
//         return template;
//     }
// }