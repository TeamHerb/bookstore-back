package com.bookwhale.post.domain;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;

import com.bookwhale.common.TestConfig;
import com.bookwhale.common.domain.Location;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

@DisplayName("게시글 단위 테스트(Repository)")
@Import(TestConfig.class)
@DataJpaTest
public class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    Post tobySpringPost;

    Post hrSpringPost;

    Post effectiveJavaPost;

    @BeforeEach
    void setUp() {
        Book tobySpring = Book.builder()
            .bookIsbn("12345678910")
            .bookTitle("토비의 스프링")
            .bookAuthor("이일민")
            .bookPublisher("허브출판사")
            .build();

        Book hrSpring = Book.builder()
            .bookIsbn("12345678911")
            .bookTitle("상우의 스프링")
            .bookAuthor("남상우")
            .bookPublisher("허브출판사")
            .build();

        Book effectiveJava = Book.builder()
            .bookIsbn("12345678912")
            .bookTitle("이펙티브 자바")
            .bookAuthor("남상우 이일민")
            .bookPublisher("한국출판사")
            .build();

        tobySpringPost = Post.builder()
            .title("토비의 스프링 팝니다.")
            .book(tobySpring)
            .sellingLocation(Location.SEOUL)
            .build();

        hrSpringPost = Post.builder()
            .title("상우의 스프링 팝니다.")
            .book(hrSpring)
            .sellingLocation(Location.BUSAN)
            .postStatus(PostStatus.SALE)
            .build();

        effectiveJavaPost = Post.builder()
            .title("이펙티브 자바 팝니다.")
            .book(effectiveJava)
            .sellingLocation(Location.SEOUL)
            .postStatus(PostStatus.SOLD_OUT)
            .build();

        postRepository.saveAll(of(tobySpringPost, hrSpringPost, effectiveJavaPost));
    }

    @DisplayName("책 제목에 스프링이 포함된 게시글들을 오름차순으로 찾는다.")
    @Test
    void findAllOrderByCreatedDateDesc_bookTitle() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Post> res = postRepository.findAllOrderByCreatedDateDesc("스프링", null, null, null, null,
                pageRequest)
            .getContent();

        Assertions.assertAll(
            () -> assertThat(res.size()).isEqualTo(2),
            () -> assertThat(res.get(0).getBook().getBookTitle()).isEqualTo(
                hrSpringPost.getBook().getBookTitle()),
            () -> assertThat(res.get(1).getBook().getBookTitle()).isEqualTo(
                tobySpringPost.getBook().getBookTitle())
        );
    }

    @DisplayName("책 저자에 남상우가 포함된 게시글들을 오름차순으로 찾는다.")
    @Test
    void findAllOrderByCreatedDateDesc_bookAuthor() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Post> res = postRepository.findAllOrderByCreatedDateDesc(null, "남상우", null, null, null,
                pageRequest)
            .getContent();

        Assertions.assertAll(
            () -> assertThat(res.size()).isEqualTo(2),
            () -> assertThat(res.get(0).getBook().getBookTitle()).isEqualTo(
                effectiveJavaPost.getBook().getBookTitle()),
            () -> assertThat(res.get(1).getBook().getBookTitle()).isEqualTo(
                hrSpringPost.getBook().getBookTitle())
        );
    }

    @DisplayName("책 출판사에 허브가 포함된 게시글들을 오름차순으로 찾는다.")
    @Test
    void findAllOrderByCreatedDateDesc_bookPublisher() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Post> res = postRepository.findAllOrderByCreatedDateDesc(null, null, "허브", null, null,
                pageRequest)
            .getContent();

        Assertions.assertAll(
            () -> assertThat(res.size()).isEqualTo(2),
            () -> assertThat(res.get(0).getBook().getBookTitle()).isEqualTo(
                hrSpringPost.getBook().getBookTitle()),
            () -> assertThat(res.get(1).getBook().getBookTitle()).isEqualTo(
                tobySpringPost.getBook().getBookTitle())
        );
    }

    @DisplayName("판매지역이 서울인 판매글들을 오름차순으로 찾는다.")
    @Test
    void findAllOrderByCreatedDateDesc_sellingLocation() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Post> res = postRepository.findAllOrderByCreatedDateDesc(null, null, null, "SEOUL",
                null,
                pageRequest)
            .getContent();

        Assertions.assertAll(
            () -> assertThat(res.size()).isEqualTo(2),
            () -> assertThat(res.get(0).getBook().getBookTitle()).isEqualTo(
                effectiveJavaPost.getBook().getBookTitle()),
            () -> assertThat(res.get(0).getSellingLocation()).isEqualTo(
                effectiveJavaPost.getSellingLocation()),
            () -> assertThat(res.get(1).getBook().getBookTitle()).isEqualTo(
                tobySpringPost.getBook().getBookTitle()),
            () -> assertThat(res.get(1).getSellingLocation()).isEqualTo(
                tobySpringPost.getSellingLocation())
        );
    }

    @DisplayName("판매지역이 서울이고 출판사가 허브출판사인 판매글들을 오름차순으로 찾는다.")
    @Test
    void findAllOrderByCreatedDateDesc_sellingLocationAndPublisher() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Post> res = postRepository.findAllOrderByCreatedDateDesc(null, null, "허브", "SEOUL",
                null,
                pageRequest)
            .getContent();

        Assertions.assertAll(
            () -> assertThat(res.size()).isEqualTo(1),
            () -> assertThat(res.get(0).getBook().getBookTitle()).isEqualTo(
                tobySpringPost.getBook().getBookTitle()),
            () -> assertThat(res.get(0).getSellingLocation()).isEqualTo(
                tobySpringPost.getSellingLocation())
        );
    }

    @DisplayName("판매완료 된 판매글들을 오름차순으로 찾는다.")
    @Test
    void findAllOrderByCreatedDateDesc_postStatus() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Post> res = postRepository.findAllOrderByCreatedDateDesc(null, null, null, null,
                "SOLD_OUT",
                pageRequest)
            .getContent();

        Assertions.assertAll(
            () -> assertThat(res.size()).isEqualTo(1),
            () -> assertThat(res.get(0).getBook().getBookTitle()).isEqualTo(
                effectiveJavaPost.getBook().getBookTitle()),
            () -> assertThat(res.get(0).getPostStatus()).isEqualTo(
                effectiveJavaPost.getPostStatus())
        );
    }

    @DisplayName("페이징이 옳바르게 작동하는지 확인한다.")
    @Test
    void findAllOrderByCreatedDateDesc_paging() {
        PageRequest pageRequest = PageRequest.of(0, 2);
        List<Post> res = postRepository.findAllOrderByCreatedDateDesc(null, "남상우", null, null, null,
                pageRequest)
            .getContent();

        Assertions.assertAll(
            () -> assertThat(res.size()).isEqualTo(2),
            () -> assertThat(res.get(0).getBook().getBookTitle()).isEqualTo(
                effectiveJavaPost.getBook().getBookTitle()),
            () -> assertThat(res.get(1).getBook().getBookTitle()).isEqualTo(
                hrSpringPost.getBook().getBookTitle())
        );
    }
}