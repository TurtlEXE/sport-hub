document.addEventListener("DOMContentLoaded", function() {
    if (typeof facilityId !== 'undefined' && facilityId > 0) {
        fetchFacilityDetail(facilityId);
    }
    
    // Tab switching logic
    const tabs = document.querySelectorAll('.border-b-2.border-transparent');
    // For simplicity, we just make them clickable visually here
    tabs.forEach(tab => {
        tab.addEventListener('click', function() {
            // Find current active tab and deactivate
            const active = document.querySelector('.border-blue-600');
            if (active) {
                active.classList.remove('border-blue-600', 'text-blue-600');
                active.classList.add('border-transparent', 'text-gray-500', 'hover:text-gray-700', 'hover:border-gray-300');
            }
            
            // Activate clicked tab
            this.classList.remove('border-transparent', 'text-gray-500', 'hover:text-gray-700', 'hover:border-gray-300');
            this.classList.add('border-blue-600', 'text-blue-600');
        });
    });
});

function fetchFacilityDetail(id) {
    fetch(`/api/owner/facilities/${id}`)
        .then(response => {
            if (!response.ok) throw new Error("Network response was not ok");
            return response.json();
        })
        .then(data => {
            renderFacilityDetail(data);
        })
        .catch(error => {
            console.error('Error:', error);
            const container = document.querySelector('.bg-white.rounded-lg.shadow.border.p-6');
            if (container) container.innerHTML = `<div class="text-red-500 p-4">Lỗi tải dữ liệu: ${error.message}</div>`;
        });
}

function renderFacilityDetail(data) {
    // Basic info render
    const infoContainer = document.querySelector('.bg-white.rounded-lg.shadow.border.p-6');
    if (!infoContainer) return;

    let statusHtml = '';
    if (data.approvalStatus === 'PENDING') statusHtml = `<span class="bg-yellow-100 text-yellow-800 px-3 py-1 rounded-full text-sm font-semibold">Chờ duyệt</span>`;
    else if (data.approvalStatus === 'APPROVED') statusHtml = `<span class="bg-green-100 text-green-800 px-3 py-1 rounded-full text-sm font-semibold">Đã duyệt</span>`;
    else statusHtml = `<span class="bg-red-100 text-red-800 px-3 py-1 rounded-full text-sm font-semibold">Từ chối</span>`;

    let rejectionAlert = '';
    if (data.approvalStatus === 'REJECTED' && data.rejectionReason) {
        rejectionAlert = `
        <div class="mb-6 p-4 bg-red-50 rounded-lg border border-red-200">
            <div class="flex">
                <div class="flex-shrink-0">
                    <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                        <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
                    </svg>
                </div>
                <div class="ml-3">
                    <h3 class="text-sm font-medium text-red-800">Cơ sở bị từ chối duyệt</h3>
                    <div class="mt-2 text-sm text-red-700">
                        <p>Lý do: ${data.rejectionReason}</p>
                    </div>
                    <div class="mt-4">
                        <div class="-mx-2 -my-1.5 flex">
                            <button type="button" onclick="resubmitFacility(${data.facilityId})" class="bg-red-50 px-2 py-1.5 rounded-md text-sm font-medium text-red-800 hover:bg-red-100 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-red-50 focus:ring-red-600">Gửi lại yêu cầu duyệt</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>`;
    }

    infoContainer.innerHTML = `
        ${rejectionAlert}
        <div class="flex justify-between items-start mb-6">
            <h3 class="text-lg font-medium">Thông tin cơ bản</h3>
            ${statusHtml}
        </div>
        
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
                <p class="text-sm text-gray-500 mb-1">Tên cơ sở</p>
                <p class="font-medium text-gray-900 text-lg">${data.name}</p>
            </div>
            
            <div class="md:col-span-2">
                <p class="text-sm text-gray-500 mb-1">Địa chỉ</p>
                <p class="font-medium text-gray-900">${data.address}, ${data.ward}, ${data.district}, ${data.province}</p>
            </div>
            
            <div>
                <p class="text-sm text-gray-500 mb-1">Giờ hoạt động</p>
                <p class="font-medium text-gray-900">${data.openTime} - ${data.closeTime}</p>
            </div>
            
            <div>
                <p class="text-sm text-gray-500 mb-1">Ngày tạo</p>
                <p class="font-medium text-gray-900">${new Date(data.createdAt).toLocaleDateString('vi-VN')}</p>
            </div>
            
            ${data.description ? `
            <div class="md:col-span-2 mt-2">
                <p class="text-sm text-gray-500 mb-1">Mô tả</p>
                <div class="text-gray-700 text-sm bg-gray-50 p-4 rounded-lg">${data.description}</div>
            </div>` : ''}
        </div>
    `;
}

function resubmitFacility(id) {
    if(confirm("Bạn có chắc chắn muốn gửi lại yêu cầu duyệt không?")) {
        fetch(`/api/owner/facilities/${id}/resubmit`, {
            method: 'POST'
        }).then(res => {
            if(res.ok) {
                alert("Đã gửi yêu cầu duyệt thành công!");
                window.location.reload();
            } else {
                alert("Có lỗi xảy ra khi gửi yêu cầu.");
            }
        });
    }
}
