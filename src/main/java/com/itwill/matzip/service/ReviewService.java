package com.itwill.matzip.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.itwill.matzip.domain.HashtagCategory;
import com.itwill.matzip.domain.Member;
import com.itwill.matzip.domain.Restaurant;
import com.itwill.matzip.domain.Review;
import com.itwill.matzip.domain.ReviewHashtag;
import com.itwill.matzip.domain.ReviewImage;
import com.itwill.matzip.domain.ReviewLike;
import com.itwill.matzip.domain.enums.HashtagCategoryName;
import com.itwill.matzip.dto.MyReviewRequestDto;
import com.itwill.matzip.dto.ReviewCreateDto;
import com.itwill.matzip.dto.ReviewLikeRegisterDto;
import com.itwill.matzip.dto.ReviewUpdateDto;
import com.itwill.matzip.repository.HashtagCategoryRepository;
import com.itwill.matzip.repository.member.MemberRepository;
import com.itwill.matzip.repository.reviewHashtag.ReviewHashtagRepository;
import com.itwill.matzip.repository.ReviewImageRepository;
import com.itwill.matzip.repository.ReviewLikeRepository;
import com.itwill.matzip.repository.review.ReviewRepository;
import com.itwill.matzip.repository.restaurant.RestaurantRepository;
import com.itwill.matzip.util.DateTimeUtil;
import com.itwill.matzip.util.S3Utility;
import com.itwill.matzip.util.SecurityUtility;

import jakarta.persistence.EntityManager;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReviewService {
    
    private final ReviewRepository reviewDao;
    private final ReviewHashtagRepository reviewHTDao;
    private final HashtagCategoryRepository hashCategoryDao;
    private final ReviewImageRepository reviewImageDao;
    private final RestaurantRepository  restaurantDao;
    private final MemberRepository memberDao; 
    private final S3Utility s3Util;
    
    private final ReviewLikeRepository reviewLikeDao;
    
    
    @Autowired
    public ReviewService(ReviewRepository reviewDao, ReviewImageRepository reviewImageDao, 
                        ReviewHashtagRepository reviewHTDao, HashtagCategoryRepository hashCategoryDao,
                        RestaurantRepository  restaurantDao, MemberRepository memberDao,
                        S3Utility s3Util, ReviewLikeRepository reviewLikeDao) {
        this.reviewDao = reviewDao;
        this.reviewImageDao = reviewImageDao;
        this.reviewHTDao = reviewHTDao;
		this.hashCategoryDao = hashCategoryDao;
		this.restaurantDao = restaurantDao;
		this.memberDao = memberDao;
        this.s3Util = s3Util;
        this.reviewLikeDao=reviewLikeDao;
    }
    
    // 리뷰 좋아요 개수
    public Long countLikesByReviewId(Long reviewId) {
        return reviewLikeDao.countByReviewId(reviewId);
    }
    
    // 리뷰 좋아요 삭제
    @Transactional
    public void deleteReviewLike(Long reviewId, Long memberId) {
        Optional<ReviewLike> reviewLikeOpt = reviewLikeDao.findByMemberIdAndReviewId(memberId, reviewId);
        reviewLikeOpt.ifPresent(reviewLike -> reviewLikeDao.delete(reviewLike));
    }


    // 리뷰 좋아요 상태 확인
    public Optional<ReviewLike> checkReviewLike(Long memberId, Long reviewId) {
        return reviewLikeDao.findByMemberIdAndReviewId(memberId, reviewId);
    }

    // 리뷰 좋아요 추가
    @Transactional
    public Long registerReviewLike(ReviewLikeRegisterDto dto) {
        Long memberId = dto.getMemberId();
        Long reviewId = dto.getReviewId();
        
        Review review = findReviewById(reviewId);
        Member member = memberDao.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다: " + memberId));
        
        ReviewLike reviewLike = new ReviewLike();
        reviewLike.setReview(review);
        reviewLike.setMember(member);
        ReviewLike savedReviewLike = reviewLikeDao.save(reviewLike);
        
        return savedReviewLike.getId();
    }


    // ReviewId 조회
    public Review findReviewById(Long reviewId) {
    	return reviewDao.findById(reviewId)
    			.orElseThrow(() -> new IllegalArgumentException("존재하지않는 리뷰 ID" + reviewId));
    }
    
    // 리뷰 삭제
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewDao.findById(reviewId)
            .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없음: " + reviewId));
        
        // 리뷰와 연관된 해시태그 관계 제거
        Set<ReviewHashtag> hashtags = review.getHashtags();
        if (hashtags != null && !hashtags.isEmpty()) {
        	Iterator<ReviewHashtag> iterator = hashtags.iterator();
        	while (iterator.hasNext()) {
        	    ReviewHashtag hashtag = iterator.next();
        	    iterator.remove(); // 반복자를 사용하여 현재 요소를 컬렉션에서 제거
        	    hashtag.getReviews().remove(review);
        	    if (hashtag.getReviews().isEmpty()) {
        	        reviewHTDao.delete(hashtag);
        	    }
        	}

        }

        // 리뷰와 연관된 이미지 삭제 (S3 및 데이터베이스에서)
        List<ReviewImage> reviewImages = review.getReviewImages();
        for (ReviewImage image : reviewImages) {
            String imageUrl = image.getImgUrl();
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            s3Util.deleteImageFromS3(fileName); // S3에서 이미지 삭제
            reviewImageDao.delete(image); // 데이터베이스에서 이미지 정보 삭제
        }

        // 모든 관계를 해제한 후 리뷰 삭제
        reviewDao.delete(review);
    }    


    
        
    
    // 리뷰 업데이트
    @Transactional
    public void updateReview(Long reviewId, ReviewUpdateDto dto) {

        Review review = findReviewById(reviewId);

        // 평점 내용 업데이트
        review.updateReview(dto.getTasteRating(), dto.getPriceRating(), dto.getServiceRating(), dto.getReviewContent());

        // 이미지 처리
        updateDeleteImages(review, dto.getImages(), dto.getDeleteImageUrls());
        
        // 해시태그 처리
        updateDeleteHashtags(review, dto.getVisitPurposeTags(), dto.getMoodTags(), dto.getConvenienceTags(), dto.getDeleteHashtagIds());
    }
    
    @Transactional
    private void updateDeleteHashtags(Review review, List<String> visitPurposeTags, List<String> moodTags, List<String> convenienceTags, List<Long> deleteHashtagIds) {
    	if (deleteHashtagIds != null && !deleteHashtagIds.isEmpty()) {
    	    for (Long hashtagId : deleteHashtagIds) {
    	        Optional<ReviewHashtag> optionalHashtag = reviewHTDao.findById(hashtagId);
    	        if (optionalHashtag.isPresent()) {
    	            ReviewHashtag hashtag = optionalHashtag.get();
    	            review.getHashtags().remove(hashtag); // 리뷰에서 해시태그 제거
    	            hashtag.getReviews().remove(review); // 해시태그에서 리뷰 제거
    	            // 해시태그가 다른 리뷰에도 연결된게 없으면 삭제
    	            if (hashtag.getReviews().isEmpty()) {
    	                reviewHTDao.delete(hashtag);
    	            }
    	        }
    	    }
    	}

        // 새로운 해시태그 처리
        saveHashtags(visitPurposeTags, HashtagCategoryName.VISIT_PURPOSE, review);
        saveHashtags(moodTags, HashtagCategoryName.MOOD, review);
        saveHashtags(convenienceTags, HashtagCategoryName.CONVENIENCE, review);
    }

    
    @Transactional
    private void updateDeleteImages(Review review, MultipartFile[] images, List<String> deleteImageUrls) {
        // 삭제 이미지 처리
        for (String imageUrl : deleteImageUrls) {
            ReviewImage reviewImage = reviewImageDao.findByImgUrl(imageUrl);
            if (reviewImage != null) {
                reviewImageDao.delete(reviewImage); // DB 이미지 삭제
                String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                s3Util.deleteImageFromS3(fileName); // S3 이미지 삭제
            }
        }

        // 새로운 이미지 처리
        if (images != null) {
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String imageUrl = s3Util.uploadImageToS3(image, s3Util.generateFileName());
                    ReviewImage newReviewImage = new ReviewImage(null, review, imageUrl);
                    reviewImageDao.save(newReviewImage);
                }
            }
        }
    }
    

    // 리뷰 저장
	@Transactional
    public void saveReview(ReviewCreateDto dto) {
    	
    	log.info("방문목적 태그: {}", dto.getVisitPurposeTags());
    	log.info("분위기 태그: {}", dto.getMoodTags());
    	log.info("편의시설 태그: {}", dto.getConvenienceTags());
    	
    	Restaurant restaurant = restaurantDao.findById(dto.getRestaurantId()) 
    			.orElseThrow(() -> new IllegalArgumentException("존재하지않는 restaurant ID: " + dto.getRestaurantId()));
    	
    	String currentUsername = SecurityUtility.getCurrentUsername();
    	Member member = memberDao.findByUsername(currentUsername)
    	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자: " + currentUsername));
    	
    	
        // 리뷰 기본 정보 
        Review review = Review.builder()
                              .flavorScore(dto.getTasteRating())
                              .priceScore(dto.getPriceRating())
                              .serviceScore(dto.getServiceRating())
                              .content(dto.getReviewContent())
                              .restaurant(restaurant)
                              .member(member)
                              .build();
        
        
        // 리뷰 저장
        Review savedReview = reviewDao.save(review);

        // 이미지 업로드..저장
		if (dto.getImages() != null && dto.getImages().length > 0) {
			for (MultipartFile image : dto.getImages()) {
				
				// 이미지 비어있지 않은지 체크
				if (!image.isEmpty()) {
					try {
						// 이미지 업로드(S3) & 업로드한 이미지 URL 받음
						String imageUrl = s3Util.uploadImageToS3(image, s3Util.generateFileName());

						ReviewImage reviewImage = new ReviewImage(null, savedReview, imageUrl);
						log.info(imageUrl);
						reviewImageDao.save(reviewImage);
					} catch (Exception e) {
						log.error("이미지 업로드 X", e);
						e.printStackTrace();
					}
				}

			}
		}
        
        // 해시태그(목적/분위기/편의시설) 각각 저장
        saveHashtags(dto.getVisitPurposeTags(), HashtagCategoryName.VISIT_PURPOSE, savedReview);
        saveHashtags(dto.getMoodTags(), HashtagCategoryName.MOOD, savedReview);
        saveHashtags(dto.getConvenienceTags(), HashtagCategoryName.CONVENIENCE, savedReview);
    }

	
    // 해시태그 저장
    private void saveHashtags(List<String> tags, HashtagCategoryName categoryEnum, Review savedReview) {
        if (tags == null || tags.isEmpty()) return; // 태그없으면

        // 카테고리 조회
        HashtagCategory category = hashCategoryDao.findByName(categoryEnum.getCategoryName()).orElse(null);

        for (String tag : tags) {
            log.info("saveHashtags() - 해시태그:{}, 태그카테고리: {}", tag, categoryEnum.getCategoryName());

            // 카테고리에 같은 키워드가 있는지 확인
            ReviewHashtag existingTag = reviewHTDao.findByKeywordAndHtCategory(tag, category)
                                                    .orElseGet(() -> {
                                                        // 같은 키워드가 없으면 새로 저장
                                                        ReviewHashtag newTag = ReviewHashtag.builder()
                                                                                             .keyword(tag)
                                                                                             .htCategory(category)
                                                                                             .build();
                                                        reviewHTDao.save(newTag);
                                                        return newTag;
                                                    });

            savedReview.getHashtags().add(existingTag); // Review에 해시태그 추가
            existingTag.getReviews().add(savedReview); // 해시태그에 Review 추가
        }
    }


    /** 회원과 연관된 리뷰 메서드 모음 ---------------------------------------*/
    //오직 리뷰 정보(리뷰 엔터티 - 맛, 서비스, 분위기 평점 이용)만 가져오기
	public List<Review> getAllReviews(Long userid) {
		List<Review> list = reviewDao.findByMemberIdOrderById(userid);
		return list;
	}
    
    
    //유저가 작성한 리뷰 목록(리뷰 및 레스토랑, 카테고리 정보) 가져오기 - 페이지 처리
    public Page<MyReviewRequestDto> getReviews(Long id, int p) {
        log.info("getReviews : member - {}", id);
        List<MyReviewRequestDto> dto = null;

        List<Review> reviews = reviewDao.findByMemberIdOrderById(id);

        if (!reviews.isEmpty()) {
            dto = new ArrayList<MyReviewRequestDto>();
            Double a = getTotalRating(reviews);

            for (Review review : reviews) {
            	
            	int reviewLikeCnt = reviewLikeDao.countAllByReviewId(review.getId());
            	
                dto.add(MyReviewRequestDto.builder()
                        .restaurantId(review.getRestaurant().getId())
                        .restaurantName(review.getRestaurant().getName())
                        .categoryName(review.getRestaurant().getCategory().getName())
                        .location(review.getRestaurant().getAddress())
                        .reviewId(review.getId())
                        .formattedRegisterDate(DateTimeUtil.formatLocalDateTime(review.getCreatedTime()))
                        .flavorScore((double) review.getFlavorScore())
                        .serviceScore((double) review.getServiceScore())
                        .priceScore((double) review.getPriceScore())
                        .content(review.getContent())
                        .mainImg(getImgUrl(review.getRestaurant().getId()))
                        .reviewImg(getReviewImg(review.getId()))
                        .totalAllReviewRating(a)
                        .reviewLikeCnt(reviewLikeCnt)
                        .build());
            }
        }
        
		PageRequest pageRequest = PageRequest.of(p, 1);
		int start = (int)pageRequest.getOffset();
		int end = Math.min((start+pageRequest.getPageSize()), dto.size());
		
		Page<MyReviewRequestDto> list = new PageImpl<>(dto.subList(start, end), pageRequest, dto.size());

        return list;
    }


    //별점 평균 구하기
    public Double getTotalRating(List<Review> list) {
        double temp = 0.0;

        for (Review review : list) {
            temp += (review.getFlavorScore() + review.getPriceScore() + review.getServiceScore()) / 3.0;
        }
        
        if(list.size() == 0) {
        	return 0.0;
        }else {
        	return Math.floor((temp / list.size())*10.0)/10.0;        	
        }
    }
	
	
    //내 리뷰에 해당하는 이미지들 가져오기
    public List<String> getReviewImg(Long reviewId) {
        List<ReviewImage> imgs = reviewImageDao.findByReviewId(reviewId);
        List<String> strImg = new ArrayList<String>();
        for (ReviewImage img : imgs) {
            strImg.add(img.getImgUrl());
        }

        return strImg;
    }
	
    //getReviews 메서드에서 사용 - 레스토랑에 해당하는 리뷰 이미지 첫번째 거 가져오기
    public String getImgUrl(Long restaurantId) {

        List<Review> reviews = reviewDao.findByRestaurantId(restaurantId);
        String rlmg = null;

        for (Review r : reviews) {
            List<ReviewImage> imgs = reviewImageDao.findByReviewId(r.getId());

            if (!imgs.isEmpty()) {
                rlmg = imgs.get(0).getImgUrl().toString();
            }
        }
        return rlmg;
    }	
	/**-----------------------------------------------------------------------------*/
}
