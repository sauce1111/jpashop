package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    EntityManager em;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;

    @Test
    @DisplayName("상품 주문")
    void testOrder() {
        // given
        Member member = createMember();
        Book book = createBook("book1", 10000, 10);

        // when
        int orderCount = 2;
        Long savedId = orderService.order(member.getId(), book.getId(), orderCount);

        // then
        Order findOrder = orderRepository.findOne(savedId);

        assertEquals(OrderStatus.ORDER, findOrder.getOrderStatus());
        assertEquals(1, findOrder.getOrderItems().size());
        assertEquals(10000 * orderCount, findOrder.getTotalPrice());
        assertEquals(8, book.getStockQuantity());
    }

    @Test
    @DisplayName("주문 취소")
    void testOrderCancel() {
        // given
        Member member = createMember();
        Item item = createBook("book1", 10000, 10);

        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        // when
        orderService.cancelOrder(orderId);

        // then
        Order order = orderRepository.findOne(orderId);
        assertEquals(OrderStatus.CANCEL, order.getOrderStatus());
        assertEquals(10, item.getStockQuantity());
    }

    @Test
    @DisplayName("상품주문 재고수량 초과")
    void testOverStockQuantity() {
        // given
        Member member = createMember();
        Item item = createBook("book1", 10000, 10);

        int orderCount = 11;
        // when, then
        Assertions.assertThrows(NotEnoughStockException.class, () -> {
            orderService.order(member.getId(), item.getId(), orderCount);
        });
    }

    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("member1");
        member.setAddress(new Address("서울", "강북", "123-123"));
        em.persist(member);
        return member;
    }
}