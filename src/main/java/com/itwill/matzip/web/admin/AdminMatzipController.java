package com.itwill.matzip.web.admin;

import com.itwill.matzip.domain.*;
import com.itwill.matzip.domain.enums.ApprovalStatus;
import com.itwill.matzip.dto.*;
import com.itwill.matzip.dto.admin.*;
import com.itwill.matzip.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Controller
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin/matzip")
public class AdminMatzipController {

    private final AdminService adminService;

    //    레스토랑 추가 페이지로 이동
    @GetMapping("/restaurant")
    public String showPageToAddMatzip(Model model) {
        List<Category> categories = adminService.getCategories();
        model.addAttribute("categories", categories);
        return "admin/create-restaurant";
    }

    //    레스토랑 메뉴 추가 페이지로 이동
    @GetMapping("/restaurant/{restaurantId}/menu")
    public String showPageToAddMenu(@PathVariable(name = "restaurantId") Long restaurantId, Model model) {
        log.info("restaurantId = {}", restaurantId);
        Restaurant restaurant = adminService.getRestaurant(restaurantId);
        model.addAttribute("restaurant", restaurant);
        return "admin/update-menu";
    }

    //    레스토랑 관리 리스트
    @GetMapping("/restaurant/all")
    public String showRestaurantListPage(RestaurantSearchCond cond, Model model) {
        log.info("showRestaurantListPage(RestaurantSearchCond cond={})", cond.getKeywordCriteria());

        Map<String, Object> result = adminService.getRestaurantByOptions(cond);
        model.addAllAttributes(result);

        log.info("restaurants={}", result.get("restaurants"));
        return "admin/restaurant-list";
    }

    //    레스토랑 추가
    @ResponseBody
    @PostMapping("/restaurant")
    public ResponseEntity<Long> addMatzip(@RequestBody RestaurantToCreateDto restaurant) {
        log.info("addMatzip(restaurant : {})", restaurant);

        Restaurant restaurantCreated = adminService.addMatzip(restaurant);

        log.info("restaurantCreated={}", restaurantCreated);
        return ResponseEntity.ok(restaurantCreated.getId());
    }

    //    레스토랑 삭제 (폐업 처리된 레스토랑 삭제)
    @ResponseBody
    @DeleteMapping("/restaurant/{restaurantId}")
    public ResponseEntity<Void> deleteMatzipData(@PathVariable(name = "restaurantId") Long restaurantId) {
        adminService.deleteRestaurantById(restaurantId);
        return ResponseEntity.noContent().build();
    }

    //    메뉴 등록하는 REST API
    @ResponseBody
    @PostMapping("/restaurant/{restaurantId}/menu")
    public ResponseEntity<URI> addMenuToRestaurant(@RequestBody MenusToCreateDto menus, @PathVariable(name = "restaurantId") Long restaurantId) {
        menus.setRestaurantId(restaurantId);

        adminService.addMenusToRestaurant(menus);
        URI url = URI.create("./");
        return ResponseEntity.created(url).build();
    }

    @ResponseBody
    @PostMapping("/restaurant/{restaurantId}/menu/one")
    public ResponseEntity<Long> addMenuToRestaurant(@RequestBody MenuToCreateDto menu, @PathVariable(name = "restaurantId") Long restaurantId) {
        log.info("addMenuToRestaurant(MenuToCreateDto={})", menu);
        Menu menuCreated = adminService.addMenuToRestaurant(restaurantId, menu);

        return ResponseEntity.ok(menuCreated.getId());
    }

    //    메뉴 삭제 REST API
    @ResponseBody
    @DeleteMapping("/restaurant/menu")
    public ResponseEntity<Void> deleteMenuToRestaurant(@RequestParam List<Long> menuId) {

        log.info("deleteMenuToRestaurant(menus={})", menuId);

        adminService.deleteMenuFromRestaurant(menuId);

        return ResponseEntity.noContent().build();
    }

    @ResponseBody
    @PatchMapping("/restaurant/menu/{menuId}/price")
    public ResponseEntity<Menu> updateMenuPriceToRestaurant(@PathVariable(name = "menuId") Long menuId, @RequestParam(name = "price") Long price) {
        Menu menu = adminService.updateMenuPrice(menuId, price);
        return ResponseEntity.ok(menu);
    }

    @ResponseBody
    @PatchMapping("/restaurant/menu/{menuId}/name")
    public ResponseEntity<Menu> updateMenuNameToRestaurant(@PathVariable(name = "menuId") Long menuId, @RequestParam(name = "name") String name) {
        Menu menu = adminService.updateMenuName(menuId, name);
        return ResponseEntity.ok(menu);
    }

    @ResponseBody
    @PutMapping("/restaurant/{restaurantId}/{status}")
    public ResponseEntity<Void> setRestaurantById(
            @PathVariable(name = "restaurantId") Long restaurantId,
            @PathVariable(name = "status") String status
    ) {
        adminService.setStatusRestaurantById(restaurantId, status);
        return ResponseEntity.noContent().build();
    }

    //    레스토랑 디테일 페이지로 이동
    @GetMapping("/restaurant/{restaurantId}")
    public String getRestaurantDetail(@PathVariable(name = "restaurantId") Long restaurantId, Model model) {
        Map<String, Object> result = adminService.getRestaurantForDetail(restaurantId);
        model.addAllAttributes(result);
        return "admin/detail-restaurant";
    }

    @GetMapping("/restaurant/{restaurantId}/info")
    public String getRestaurantUpdatePage(@PathVariable(name = "restaurantId") Long restaurantId, Model model) {

        Map<String, Object> result = adminService.getRestaurantInfoForUpdate(restaurantId);
        model.addAllAttributes(result);
        return "admin/update-restaurant";
    }

    @ResponseBody
    @PatchMapping("/restaurant/{restaurantId}")
    public ResponseEntity<Restaurant> updateRestaurantInfo(@RequestBody() RestaurantUpdateDto restaurantUpdateDto) {
        Restaurant updatedRestaurant = adminService.updateRestaurant(restaurantUpdateDto);
        return ResponseEntity.ok(updatedRestaurant);
    }

    @ResponseBody
    @PatchMapping("/restaurant/{restaurantId}/time")
    public ResponseEntity<String> updateRestaurantBusinessTime(@RequestBody List<BusinessHourUpdateDto> businessHours, @PathVariable(name = "restaurantId") Long restaurantId) {
        log.info("businessHours={}", businessHours);
        log.info("restaurantId={}", restaurantId);

        adminService.updateRestaurantBusinessTime(businessHours, restaurantId);
        return ResponseEntity.ok("updated");
    }

    @GetMapping("/category")
    public String moveToControlCategory(Model model) {
        List<Category> categories = adminService.getCategories();
        model.addAttribute("categories", categories);
        log.info("categories={}", categories);
        return "admin/update-category";
    }

    @ResponseBody
    @GetMapping("/category/all")
    public ResponseEntity<List<CategoryListDto>> getCategories() {
        List<CategoryListDto> categoryListItems = adminService.getCategoryListItems();

        return ResponseEntity.ok(categoryListItems);
    }

    @ResponseBody
    @PostMapping("/category")
    public ResponseEntity<Category> createCategory(@RequestParam(name = "categoryName") String categoryName) {
        log.info("categoryNamecategoryName={}", categoryName);
        Category category = adminService.createCategory(categoryName);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @ResponseBody
    @PatchMapping("/category/order")
    public ResponseEntity<String> updateCategoryOrder(@RequestBody List<CategoryOrderUpdateDto> categoryOrderUpdateDto) {
        adminService.updateCategoryOrder(categoryOrderUpdateDto);
        return ResponseEntity.ok("order_updated");
    }

    @ResponseBody
    @DeleteMapping("/category/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable(name = "categoryId") Integer categoryId) {
        adminService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

    @ResponseBody
    @PatchMapping("/category/{categoryId}")
    public ResponseEntity<String> updateCategoryName(@PathVariable(name = "categoryId") Integer categoryId, @RequestParam(name = "categoryName") String categoryName) {
        log.info("categoryId= {}, @RequestParam(name ={}", categoryId, categoryName);
        adminService.updateCategoryName(categoryId, categoryName);
        return ResponseEntity.ok("order_updated");
    }

    @GetMapping("/hashtag/control")
    public String getHashtagControlPage() {
        return "admin/hashtag";
    }

    @ResponseBody
    @GetMapping("/hashtag/categories")
    public ResponseEntity<List<HashtagCategory>> getHashtagCategory() {
        List<HashtagCategory> hashtagCategories = adminService.getHashtagCategories();
        return ResponseEntity.ok(hashtagCategories);
    }

    @ResponseBody
    @GetMapping("/hashtag")
    public ResponseEntity<List<ReviewHashtag>> getHashtagCategory(HashtagSearchDto searchDto) {
        log.info("HashtagSearchDto={}", searchDto);
        List<ReviewHashtag> reviewHashtags = adminService.getReviewHashtags(searchDto);
        return ResponseEntity.ok(reviewHashtags);
    }

    @ResponseBody
    @GetMapping("/hashtag/{tagId}")
    public ResponseEntity<ReviewHashtag> getHashtagCategoryById(@PathVariable(name = "tagId") Long tagId) {
        ReviewHashtag hashtag = adminService.getReviewHashtagById(tagId);
        return ResponseEntity.ok(hashtag);
    }

    @ResponseBody
    @PatchMapping("/hashtag/{tagId}")
    public ResponseEntity<String> updateHashtagCategoryById(@PathVariable Long tagId, @RequestBody HashtagUpdateDto updateDto) {
        log.info("updateDto={}", updateDto);
        adminService.updateHashtag(tagId, updateDto);
        return ResponseEntity.ok("updated");
    }

    @ResponseBody
    @DeleteMapping("/hashtag")
    public ResponseEntity<Void> deleteHashtag(@RequestParam(name = "tagId") Long... tagId) {
        adminService.deleteReviewHashtagById(tagId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/request")
    public String getUpdateRequest(Model model) {
        ApprovalStatus[] approvalStatuses = ApprovalStatus.values();
        model.addAttribute("approvalStatuses", approvalStatuses);
        return "admin/request-list.html";
    }

    @ResponseBody
    @GetMapping("/requests")
    public ResponseEntity<Page<UpdateRequest>> getRequestList(SearchRequestDto searchRequestDto) {
        log.info("searchRequestDto={}", searchRequestDto);
        Page<UpdateRequest> list = adminService.getRequests(searchRequestDto);
        return ResponseEntity.ok(list);
    }

    @ResponseBody
    @PatchMapping("/requests")
    public ResponseEntity<Object> updateRequest(@RequestParam(name = "reqId") Long... reqId) {
        adminService.updateRequest(reqId);
        return ResponseEntity.ok(null);
    }

    @ResponseBody
    @GetMapping("/requests/{reqId}")
    public ResponseEntity<UpdateRequest> getRequestList(@PathVariable(name = "reqId") Long reqId) {
        UpdateRequest updateRequest = adminService.getRequestById(reqId);
        return ResponseEntity.ok(updateRequest);
    }

}
