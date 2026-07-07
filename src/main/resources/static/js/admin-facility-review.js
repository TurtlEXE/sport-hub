document.addEventListener("DOMContentLoaded", function() {
    if (typeof facilityId !== 'undefined' && facilityId > 0) {
        fetchFacilityDetail(facilityId);
    } else {
        document.getElementById('facilityInfo').innerHTML = '<div class="text-red-500">Invalid Facility ID</div>';
    }
});

function fetchFacilityDetail(id) {
    fetch(`/api/admin/facilities/${id}`)
        .then(response => {
            if (!response.ok) throw new Error("Network response was not ok");
            return response.json();
        })
        .then(data => {
            renderFacilityDetail(data);
            setupActions(data);
        })
        .catch(error => {
            console.error('Error:', error);
            document.getElementById('facilityInfo').innerHTML = `<div class="text-red-500">Lỗi tải dữ liệu: ${error.message}</div>`;
        });
}

function renderFacilityDetail(data) {
    const infoContainer = document.getElementById('facilityInfo');
    if (!infoContainer) return;

    let statusHtml = '';
    if (data.approvalStatus === 'PENDING') statusHtml = `<span class="bg-yellow-100 text-yellow-800 px-3 py-1 rounded-full text-sm font-semibold">Chờ duyệt</span>`;
    else if (data.approvalStatus === 'APPROVED') statusHtml = `<span class="bg-green-100 text-green-800 px-3 py-1 rounded-full text-sm font-semibold">Đã duyệt</span>`;
    else statusHtml = `<span class="bg-red-100 text-red-800 px-3 py-1 rounded-full text-sm font-semibold">Từ chối</span>`;

    infoContainer.innerHTML = `
        <div class="grid grid-cols-2 gap-4 mb-6">
            <div>
                <p class="text-sm text-gray-500">Tên cơ sở</p>
                <p class="font-medium text-lg">${data.name}</p>
            </div>
            <div>
                <p class="text-sm text-gray-500">Trạng thái</p>
                <p class="font-medium">${statusHtml}</p>
            </div>
            <div class="col-span-2">
                <p class="text-sm text-gray-500">Địa chỉ</p>
                <p class="font-medium">${data.address}, ${data.ward}, ${data.district}, ${data.province}</p>
            </div>
            <div>
                <p class="text-sm text-gray-500">Giờ hoạt động</p>
                <p class="font-medium">${data.openTime} - ${data.closeTime}</p>
            </div>
            <div>
                <p class="text-sm text-gray-500">Ngày tạo</p>
                <p class="font-medium">${new Date(data.createdAt).toLocaleDateString('vi-VN')}</p>
            </div>
        </div>
        
        <div class="border-t pt-4">
            <h4 class="font-medium mb-3">Thông tin chủ sân</h4>
            <div class="grid grid-cols-2 gap-4">
                <div>
                    <p class="text-sm text-gray-500">Họ tên</p>
                    <p class="font-medium">${data.ownerName || 'Chưa cập nhật'}</p>
                </div>
                <div>
                    <p class="text-sm text-gray-500">Điện thoại</p>
                    <p class="font-medium">${data.ownerPhone || 'Chưa cập nhật'}</p>
                </div>
                <div class="col-span-2">
                    <p class="text-sm text-gray-500">Email</p>
                    <p class="font-medium">${data.ownerEmail || 'Chưa cập nhật'}</p>
                </div>
            </div>
        </div>
        
        ${data.rejectionReason ? `
        <div class="mt-6 p-4 bg-red-50 rounded-lg border border-red-200">
            <p class="text-red-800 font-medium mb-1">Lý do từ chối trước đó:</p>
            <p class="text-red-600">${data.rejectionReason}</p>
        </div>` : ''}
    `;
}

function setupActions(data) {
    const actionContainer = document.getElementById('actionContainer');
    const statusContainer = document.getElementById('statusContainer');
    
    if (data.approvalStatus === 'PENDING') {
        actionContainer.classList.remove('hidden');
        statusContainer.innerHTML = '';
        
        document.getElementById('btnApprove').onclick = () => {
            if(confirm("Bạn chắc chắn muốn duyệt cơ sở này?")) {
                fetch(`/api/admin/facilities/${data.facilityId}/approve`, {
                    method: 'POST'
                }).then(res => {
                    if(res.ok) window.location.reload();
                    else alert("Có lỗi xảy ra");
                });
            }
        };

        const rejectModal = document.getElementById('rejectModal');
        document.getElementById('btnReject').onclick = () => rejectModal.classList.remove('hidden');
        document.getElementById('btnCancelReject').onclick = () => rejectModal.classList.add('hidden');
        
        document.getElementById('btnConfirmReject').onclick = () => {
            const reason = document.getElementById('rejectionReason').value;
            if(!reason) return alert("Vui lòng nhập lý do từ chối");
            
            fetch(`/api/admin/facilities/${data.facilityId}/reject`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ rejectionReason: reason })
            }).then(res => {
                if(res.ok) window.location.reload();
                else alert("Có lỗi xảy ra");
            });
        };
    } else {
        actionContainer.classList.add('hidden');
        statusContainer.innerHTML = `
            <div class="p-4 bg-gray-50 rounded-lg text-center">
                <p class="text-gray-600">Cơ sở này đã được xử lý.</p>
                <p class="font-medium mt-1">Trạng thái hiện tại: ${data.approvalStatus === 'APPROVED' ? 'Đã duyệt' : 'Đã từ chối'}</p>
            </div>
        `;
    }
}
