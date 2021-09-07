package com.teamherb.bookstoreback.user.controller;

import com.teamherb.bookstoreback.basket.dto.BasketResponse;
import com.teamherb.bookstoreback.orders.dto.PurchaseOrder;
import com.teamherb.bookstoreback.orders.dto.SaleOrder;
import com.teamherb.bookstoreback.post.dto.SalePostResponse;
import com.teamherb.bookstoreback.purchase.dto.PurchaseResponse;
import com.teamherb.bookstoreback.sale.dto.SaleResponse;
import com.teamherb.bookstoreback.security.CurrentUser;
import com.teamherb.bookstoreback.user.domain.User;
import com.teamherb.bookstoreback.user.dto.SignUpRequest;
import com.teamherb.bookstoreback.user.dto.UserResponse;
import com.teamherb.bookstoreback.user.dto.UserUpdateRequest;
import com.teamherb.bookstoreback.user.service.UserService;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping("/signup")
  public ResponseEntity<Void> signup(@Valid @RequestBody SignUpRequest signUpRequest)
      throws URISyntaxException {
    userService.createUser(signUpRequest);
    return ResponseEntity.created(new URI("/api/user/login")).build();
  }

  @GetMapping("/me")
  public ResponseEntity<UserResponse> getMyInfo(@CurrentUser User user) {
    return ResponseEntity.ok(UserResponse.of(user));
  }

  @PatchMapping("/me")
  public ResponseEntity<Void> updateMyInfo(@CurrentUser User user,
      @RequestBody UserUpdateRequest userUpdateRequest) {
    userService.updateMyInfo(user, userUpdateRequest);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/purchase-history")
  public ResponseEntity<List<PurchaseResponse>> findPurchaseHistories(@CurrentUser User user) {
    List<PurchaseResponse> res = userService.findPurchaseHistories(user);
    return ResponseEntity.ok(res);
  }

  @GetMapping("/sale-history")
  public ResponseEntity<List<SaleResponse>> findSaleHistories(@CurrentUser User user) {
    List<SaleResponse> res = userService.findSaleHistories(user);
    return ResponseEntity.ok(res);
  }

  @GetMapping("/saleOrders")
  public ResponseEntity<List<SaleOrder>> findSaleOrders(@CurrentUser User user) {
    List<SaleOrder> res = userService.findSaleOrders(user);
    return ResponseEntity.ok(res);
  }

  @GetMapping("/salePosts")
  public ResponseEntity<List<SalePostResponse>> findSalePosts(@CurrentUser User user) {
    List<SalePostResponse> res = userService.findSalePosts(user);
    return ResponseEntity.ok(res);
  }

  @GetMapping("/baskets")
  public ResponseEntity<List<BasketResponse>> findBaskets(@CurrentUser User user) {
    List<BasketResponse> res = userService.findBaskets(user);
    return ResponseEntity.ok(res);
  }

}
