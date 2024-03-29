document.addEventListener("DOMContentLoaded", function() {
    initializeStarRatings();
    
    document.getElementById("review").placeholder = 
     "방문한 식당의 솔직한 후기를 입력해 주세요!\n상세한 리뷰를 작성하고 좋아요를 받고 \n탑리뷰어에 도전해보세요!\n\n*부적절한 내용은 관리자에 의해 수정/삭제 될 수 있습니다.";
    
    
    // 폼 제출 상태를 추적하는 변수
    let formSubmitted = false;

    document.getElementById("reviewForm").addEventListener("submit", function() {
        // 폼이 제출되면 formSubmitted 플래그를 true로 설정
        formSubmitted = true;
    });
    
    // 수정 중 페이지 벗어날때 경고창
    window.addEventListener('beforeunload', function(e) {
        // 폼이 제출되지 않았고 변경사항이 있을 경우에만 사용자에게 경고 표시
        if (!formSubmitted) {
            e.returnValue = '수정을 취소하시겠습니까? 수정한 내용은 저장되지 않습니다.';
            return e.returnValue;
        }
    });
    
/* 이미지 */
    const imageInput = document.getElementById('image-input');
    const imagePreviewContainer = document.querySelector('.image-preview-container');
    let selectedFiles = [];
    let deleteImageRequests = []; // 삭제할 이미지 배열
    
    // HTML에서 th:inline="javascript"를 통해 전달받은 reviewImages 사용
    if (window.reviewImages && window.reviewImages.length > 0) {
        updateImagePreviewsWithExistingImages(window.reviewImages);
    }
    
    // 서버에 저장된 이미지 미리보기에 추가
    function updateImagePreviewsWithExistingImages(imageUrls) {
        const uploadText = document.querySelector('.image-preview-text');
        if (uploadText) {
            uploadText.style.display = 'none'; // 이미지미리보기 있으면 텍스트 숨김
        }
    
        imageUrls.forEach((imageUrl, index) => {
            const imagePreviewWrap = document.createElement('div');
            imagePreviewWrap.classList.add('image-preview-wrap');
    
            const imagePreview = document.createElement('img');
            imagePreview.src = imageUrl;
    
            // 삭제 버튼 추가
            const deleteButton = document.createElement('button');
            deleteButton.textContent = 'X';
            deleteButton.classList.add('delete-image');
            deleteButton.setAttribute('type', 'button'); // 폼 제출 방지
            deleteButton.onclick = function() {
                const confirmed = confirm("해당 이미지를 삭제하시겠습니까?\n" + imageUrl);
                if (confirmed) {
                    deleteImageRequests.push(imageUrl); 
                    imagePreviewWrap.remove(); // 미리보기에서 이미지 삭제
                    addDeletedImagesToForm();
                    // 모든 이미지가 삭제되었는지 확인하고 "이미지 업로드" 텍스트 다시 표시
                    if (imagePreviewContainer.querySelectorAll('.image-preview-wrap').length === 0) {
                        if (uploadText) {
                            uploadText.style.display = 'block';
                        }
                    }
                }
            };
    
            imagePreviewWrap.appendChild(imagePreview);
            imagePreviewWrap.appendChild(deleteButton); // 삭제 버튼을 미리보기에 추가
    
            imagePreviewContainer.appendChild(imagePreviewWrap);
        });
    }

    
    

    
    // 이미 선택된 파일들 DataTransfer 객체 사용해서 input files 속성에 설정
    function updateInputFiles(files) {
        const dataTransfer = new DataTransfer();
        files.forEach(file => {
            dataTransfer.items.add(file);
        });
        imageInput.files = dataTransfer.files;
    }

    // 이미지 미리보기에서 파일 삭제
    function removeFileFromInput(indexToRemove) {
        selectedFiles = selectedFiles.filter((_, index) => index !== indexToRemove);
        updateInputFiles(selectedFiles); // input의 files 속성 업데이트
    }

    // 미리보기를 업데이트하는 함수
    function updateImagePreviews(files , isNewFile = false) {
        // 이미지 업데이트하면 "이미지 업로드" 텍스트 숨김
        const uploadText = document.querySelector('.image-preview-text');
        if (uploadText) {
            uploadText.style.display = 'none';
        }
    
        files.forEach((file, index) => {
            if(isNewFile && document.querySelector(`[data-name="${file.name}"]`)) {
                // 이미 미리보기가 존재하는 파일은 건너뛰기
                return;
            }
            const reader = new FileReader();
            reader.onload = function(e) {
                const imagePreviewWrap = document.createElement('div');
                imagePreviewWrap.classList.add('image-preview-wrap');
                imagePreviewWrap.setAttribute('data-name', file.name); // 파일 이름으로 데이터속성 추가
    
                const imagePreview = document.createElement('img');
                imagePreview.src = e.target.result;
    
                // 삭제 버튼 추가
                const deleteButton = document.createElement('button');
                deleteButton.textContent = 'X';
                deleteButton.classList.add('delete-image');
                deleteButton.setAttribute('type', 'button'); // 폼 제출 방지
                deleteButton.onclick = function() {
                    // 선택된 파일 목록에서 이미지 삭제
                    removeFileFromInput(index);
                    // 미리보기에서 이미지 삭제
                    imagePreviewWrap.remove();
                    // 미리보기 이미지 다 삭제되면 "이미지 업로드" 텍스트 다시 표시
                    if (imagePreviewContainer.querySelectorAll('.image-preview-wrap').length === 0) {
                        if (uploadText) {
                            uploadText.style.display = 'block';
                        }
                    }
                };
    
                imagePreviewWrap.appendChild(imagePreview);
                imagePreviewWrap.appendChild(deleteButton); // 삭제 버튼 미리보기에 추가
    
                imagePreviewContainer.appendChild(imagePreviewWrap);
            };
            reader.readAsDataURL(file);
        });
    }


    imageInput.addEventListener('change', function(event) {
        // 파일이 선택됐는지 확인
        if (event.target.files.length === 0) {
            return;
        }
    
        const newFiles = Array.from(event.target.files); // 새로 선택한 파일들
    
        newFiles.forEach(file => {
            if (!selectedFiles.find(f => f.name === file.name && f.size === file.size)) {
                selectedFiles.push(file);
                // 새로 추가된 파일 미리보기 업뎃
                updateImagePreviews([file], true); // -> true는 새파일
            }
        });
    
        updateInputFiles(selectedFiles); // input의 files 속성 업데이트
    });


    /* 키워드(해시태그) */
    
    
    let deleteHashtagRequests = []; // 삭제할 해시태그 배열
    
    // 태그 ID(삭제 시 필요)
    let tagIdCounter = 0;

    document.querySelectorAll('.tag-input').forEach(input => {
        input.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault(); // 폼 제출 방지
                const tagValue = this.value.trim();
                if (tagValue) {
                    const category = this.getAttribute('data-category');
                    const tagListId = category + '-tags'; // 카테고리에 해당하는 태그 리스트의 ID
                    const tagList = document.getElementById(tagListId);
    
                    // 동일한 내용의 태그가 이미 리스트에 있는지 확인
                    const isDuplicate = Array.from(tagList.querySelectorAll('li')).some(tagItem => tagItem.textContent.replace('X', '').trim() === tagValue);
                    if (isDuplicate) {
                        alert('동일한 키워드가 이미 존재합니다.');
                        this.value = ''; 
                        return; // 
                    }
    
                    // 태그 고유 ID 생성을 위한 카운터
                    const tagId = `tag-${tagIdCounter++}`;
    
                    // 숨겨진 입력 필드 생성
                    const hiddenInput = document.createElement('input');
                    hiddenInput.type = 'hidden';
                    let inputName = '';
                    if (category === 'visit-purpose') {
                        inputName = 'visitPurposeTags[]';
                    } else if (category === 'mood') {
                        inputName = 'moodTags[]';
                    } else if (category === 'convenience') {
                        inputName = 'convenienceTags[]';
                    }
                    hiddenInput.name = inputName;
                    hiddenInput.value = tagValue;
                    hiddenInput.id = tagId;
    
                    document.getElementById('reviewForm').appendChild(hiddenInput);
    
                    // 태그 UI 생성
                    const tagItem = document.createElement('li');
                    tagItem.textContent = tagValue;
                    const deleteBtn = document.createElement('button');
                    deleteBtn.textContent = 'X';
                    deleteBtn.addEventListener('click', function() {
                        tagItem.remove(); // 태그 UI 삭제
                        document.getElementById(tagId).remove(); // 숨겨진 입력 필드 삭제
                    });
    
                    tagItem.appendChild(deleteBtn);
                    tagList.appendChild(tagItem);
    
                    this.value = ''; // 입력 필드 초기화
                }
            }
        });
    });




    document.querySelectorAll('.tag-input').forEach(function(input) {
        const maxLength = 8; // 글자 수 제한
        input.addEventListener('input', function() {
            if (this.value.length > maxLength) {
                // 글자수 제한 초과시 초과 부분 자름
                this.value = this.value.substring(0, maxLength);
                // 경고 표시
                displayWarning(this.name, '태그는 8글자를 초과할 수 없습니다.');
            } else {
                // 경고 숨김
                clearWarning(this.name);
            }
        });
    });
    
    
    hashtags.forEach(hashtag => {
        // 'hashtagCategory' 필드를 사용하여 태그 리스트 선택
        const tagList = document.getElementById(hashtag.hashtagCategory + '-tags');
        if (!tagList) {
            return; // 해당 카테고리의 태그 리스트가 없으면 다음 해시태그로 넘어감
        }
    
        const tagItem = document.createElement('li');
        tagItem.textContent = hashtag.keyword;
    
        // 숨겨진 입력 필드 생성 (해시태그 ID 저장 용도)
        const hiddenInput = document.createElement('input');
        hiddenInput.setAttribute('type', 'hidden');
        hiddenInput.setAttribute('name', 'hashtags[]'); // hiddenInput의 hashtags[]는 기존에 저장된 해시태그들
        hiddenInput.value = hashtag.hashtagId;
        tagItem.appendChild(hiddenInput);
    
        // 삭제 버튼 추가
        const deleteButton = document.createElement('button');
        deleteButton.textContent = 'X';
        deleteButton.addEventListener('click', function() {
            tagItem.remove(); // 태그 아이템 삭제
            deleteHashtagRequests.push(hiddenInput.value);
            addDeletedHashtagsToForm();
        });
    
        tagItem.appendChild(deleteButton);
        tagList.appendChild(tagItem);
    });




/*
 * 함수들 
 */ 

    function addDeletedHashtagsToForm() {
    const form = document.getElementById('reviewForm');
    deleteHashtagRequests.forEach(hashtagId => {
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'deleteHashtagIds[]'; // 여기서 input.name을 'deleteHashtags'에서 'deleteHashtags[]'로 변경, 서버 측에서 배열로 받기 위함
        input.value = hashtagId;
        form.appendChild(input);
    });

    // 해시태그 ID를 폼에 추가한 후에 deleteHashtagRequests 배열 비우기
    deleteHashtagRequests = []; // 배열을 비워서 이전에 추가된 요소가 다시 추가되지 않도록 함
}

    

    function addDeletedImagesToForm() {
        const form = document.getElementById('reviewForm');
        deleteImageRequests.forEach(imageUrl => {
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'deleteImageUrls';
            input.value = imageUrl;
            form.appendChild(input);
        });
    }

   
    function initializeStarRatings() {
        document.querySelectorAll(".rating-container").forEach(container => {
            let currentRating = parseInt(container.getAttribute('data-rating'));
            updateStars(container, currentRating);
    
            container.querySelectorAll(".star").forEach(star => {
                star.addEventListener("mouseover", () => highlightStarsOnHover(container, star));
                star.addEventListener("mouseout", () => updateStars(container, currentRating));
                star.addEventListener("click", () => {
                    currentRating = parseInt(star.getAttribute('data-value'));
                    container.setAttribute('data-rating', currentRating.toString());
                    updateStars(container, currentRating);
                    updateHiddenInput(container.id, currentRating);
                });
            });
        });
    }
    
    // 별 평점 마우스 오버시 active
    function highlightStarsOnHover(container, hoveredStar) {
        const hoverRating = parseInt(hoveredStar.getAttribute('data-value'));
        container.querySelectorAll(".star").forEach(star => {
            const starValue = parseInt(star.getAttribute('data-value'));
            star.classList.toggle("active", starValue <= hoverRating);
        });
    }
    
    // 별 평점 클릭시 해당 값으로 active 상태 변경
    function updateStars(container, rating) {
        container.querySelectorAll(".star").forEach(star => {
            const starValue = parseInt(star.getAttribute('data-value'));
            star.classList.toggle("active", starValue <= rating);
        });
    }
    
    // 별 평점 클릭시 hidden Input 값 변경
    function updateHiddenInput(containerId, rating) {
        const ratingInput = document.getElementById(`${containerId}Rating`);
        if (ratingInput) {
            ratingInput.value = rating;
        }
    }
    
    //해시태그 글자수제한
    function displayWarning(inputName, message) {
        const warningElement = document.getElementById(inputName + '-warning');
        if (warningElement) {
            warningElement.textContent = message;
            warningElement.classList.remove('d-none');
        } else {
            // 경고요소 없으면
            const inputElement = document.querySelector(`input[name=${inputName}]`);
            const newWarning = document.createElement('small');
            newWarning.id = inputName + '-warning';
            newWarning.textContent = message;
            newWarning.classList.add('form-text', 'text-muted');
            inputElement.parentNode.insertBefore(newWarning, inputElement.nextSibling);
        }
    }
    
    function clearWarning(inputName) {
        const warningElement = document.getElementById(inputName + '-warning');
        if (warningElement) {
            warningElement.classList.add('d-none');
        }
    }

    
}); // end DOM