package com.itwill.matzip.repository.reviewHashtag;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.itwill.matzip.domain.HashtagCategory;
import com.itwill.matzip.domain.Restaurant;
import com.itwill.matzip.domain.Review;
import com.itwill.matzip.domain.ReviewHashtag;
import com.itwill.matzip.domain.enums.Expose;

import jakarta.transaction.Transactional;

public interface ReviewHashtagRepository extends JpaRepository<ReviewHashtag, Long> , ReviewHashtagQuerydsl{

	// 해시태그 카테고리, 키워드로 중복체크
	Optional<ReviewHashtag> findByKeywordAndHtCategory(String keyword, HashtagCategory htCategory);
	
	List<ReviewHashtag> findByExposeOrderById(Expose e);
	
	@Query("SELECT rh FROM ReviewHashtag rh JOIN rh.reviews r WHERE r.restaurant.id = :restaurantId")
	List<ReviewHashtag> findAllByRestaurantId(@Param("restaurantId") Long restaurantId);
	
	List<ReviewHashtag> searchByKeyword(String keyword);
	
	List<ReviewHashtag> searchByCategoryAndKeyword(Long categoryId, String keyword);
	
}
