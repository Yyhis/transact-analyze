document.addEventListener("DOMContentLoaded", function () {
    const uploadArea = document.getElementById("upload-area");
    const fileInput = document.getElementById("file-input");
    const fileList = document.getElementById("file-list");
    const startKeywordInput = document.getElementById("start-keyword");
    const priceRangeSelect = document.getElementById("price-range");
    const uploadedFiles = [];

    // 드래그 앤 드롭 이벤트
    uploadArea.addEventListener("dragover", (event) => {
        event.preventDefault();
        uploadArea.classList.add("drag-over");
    });

    uploadArea.addEventListener("dragleave", (event) => {
        event.preventDefault();
        uploadArea.classList.remove("drag-over");
    });

    uploadArea.addEventListener("drop", (event) => {
        event.preventDefault();
        uploadArea.classList.remove("drag-over");
        const files = event.dataTransfer.files;
        handleFiles(files);
    });

    // 파일 선택 이벤트
    uploadArea.addEventListener("click", () => {
        fileInput.click();
    });

    fileInput.addEventListener("change", (event) => {
        const files = fileInput.files;
        handleFiles(files);
    });

    function handleFiles(files) {
        // 시작 키워드 검증
        if (!startKeywordInput.value.trim()) {
            alert('시작 키워드를 입력해주세요.');
            return;
        }

        for (let i = 0; i < files.length; i++) {
            const file = files[i];
            if (file.type !== 'application/pdf') {
                alert('PDF 파일만 업로드 가능합니다.');
                continue;
            }

            const formData = new FormData();
            formData.append("file", file);
            formData.append("priceRange", priceRangeSelect.value);
            formData.append("startKeyword", startKeywordInput.value.trim());
            uploadFile(formData, file.name);
        }
    }

    function uploadFile(formData, fileName) {
        // 파일 확장자 제거하고 엑셀 확장자 추가
        const excelFileName = fileName.replace('.pdf', '');
        formData.append("fileName", excelFileName);

        fetch('/api/upload', {
            method: 'POST',
            body: formData
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Upload failed');
            }
            return response.text();
        })
        .then(result => {
            uploadedFiles.push(fileName);
            updateFileList();
            alert('파일 업로드 성공');
        })
        .catch(error => {
            console.error("File upload failed:", error);
            alert('파일 업로드 실패');
        });
    }

    function updateFileList() {
        fileList.innerHTML = "";
        uploadedFiles.forEach(fileName => {
            const fileItem = document.createElement("div");
            fileItem.classList.add("file-item");
            fileItem.textContent = fileName;
            fileList.appendChild(fileItem);
        });
    }
});