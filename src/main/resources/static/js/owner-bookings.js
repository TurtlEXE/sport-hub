let currentFacilitySportId = null;
let currentFacilityId = null;
let currentDateObj = new Date();
let currentDate = currentDateObj.toISOString().split('T')[0];

let timelineGrid = null;
let loadingOverlay = null;
let currentDateDisplay = null;
let facilityTabsContainer = null;
let sportTabsContainer = null;

let facilitiesMap = {};
let facilitiesList = [];

let selectedSlots = []; // { courtId, courtName, startTime, endTime, price }
let facilityProducts = [];
let selectedProductsMap = {}; // productId -> qty
let fpInstance = null;

document.addEventListener('DOMContentLoaded', function () {
    timelineGrid = document.getElementById('timelineGrid');
    loadingOverlay = document.getElementById('loadingOverlay');
    currentDateDisplay = document.getElementById('currentDateDisplay');
    facilityTabsContainer = document.getElementById('facilityTabsContainer');
    sportTabsContainer = document.getElementById('sportTabsContainer');

    parseFacilitiesData();
    initDatePicker();
    updateDateDisplay(currentDateObj);
    initTabs();
});

function parseFacilitiesData() {
    const fsDataElements = document.querySelectorAll('.fs-item');
    facilitiesMap = {};
    fsDataElements.forEach(el => {
        const fsId = el.dataset.fsId;
        const fId = el.dataset.facilityId;
        const fName = el.dataset.facilityName;
        const sId = el.dataset.sportId;
        const sName = el.dataset.sportName;

        if (!facilitiesMap[fId]) {
            facilitiesMap[fId] = { id: fId, name: fName, sports: [] };
        }
        facilitiesMap[fId].sports.push({ fsId: fsId, id: sId, name: sName });
    });
    facilitiesList = Object.values(facilitiesMap);
}

function initTabs() {
    if (!facilityTabsContainer) return;
    facilityTabsContainer.innerHTML = '';

    if (facilitiesList.length === 0) {
        facilityTabsContainer.innerHTML = '<div class="px-6 py-4 text-sm text-slate-500 italic">No active facilities or sports found.</div>';
        return;
    }

    // Render Facility Tabs
    facilitiesList.forEach((fac, index) => {
        const btn = document.createElement('button');
        btn.className = 'folder-tab fac-tab';
        if (index === 0) {
            btn.classList.add('folder-tab-active');
            currentFacilityId = fac.id;
        } else {
            btn.classList.add('folder-tab-inactive');
        }
        btn.textContent = fac.name;
        btn.onclick = () => switchFacilityTab(fac.id, btn);
        facilityTabsContainer.appendChild(btn);
    });

    if (currentFacilityId) {
        renderSportTabs(currentFacilityId);
    }
}

function switchFacilityTab(facilityId, btn) {
    document.querySelectorAll('.fac-tab').forEach(el => {
        el.classList.remove('folder-tab-active');
        el.classList.add('folder-tab-inactive');
    });

    btn.classList.remove('folder-tab-inactive');
    btn.classList.add('folder-tab-active');

    currentFacilityId = facilityId;
    renderSportTabs(facilityId);
}

function renderSportTabs(facilityId) {
    if (!sportTabsContainer) return;
    sportTabsContainer.innerHTML = '';
    const fac = facilitiesMap[facilityId];
    if (!fac || fac.sports.length === 0) return;

    fac.sports.forEach((sport, index) => {
        const btn = document.createElement('button');
        btn.className = 'folder-tab sport-tab';
        btn.style.padding = '8px 20px';

        if (index === 0) {
            btn.classList.add('folder-tab-active');
            currentFacilitySportId = sport.fsId;
        } else {
            btn.classList.add('folder-tab-inactive');
        }
        btn.textContent = sport.name;
        btn.onclick = () => switchSportTab(sport.fsId, btn);
        sportTabsContainer.appendChild(btn);
    });

    if (currentFacilitySportId) {
        loadTimelineData();
    }
}

function switchSportTab(fsId, btn) {
    document.querySelectorAll('.sport-tab').forEach(el => {
        el.classList.remove('folder-tab-active');
        el.classList.add('folder-tab-inactive');
    });
    btn.classList.remove('folder-tab-inactive');
    btn.classList.add('folder-tab-active');

    currentFacilitySportId = fsId;
    loadTimelineData();
}

function initDatePicker() {
    try {
        fpInstance = flatpickr("#datePickerInput", {
            locale: "vn",
            dateFormat: "Y-m-d",
            defaultDate: currentDate,
            onChange: function (selectedDates, dateStr, instance) {
                if (selectedDates.length > 0) {
                    currentDateObj = selectedDates[0];
                    currentDate = dateStr;
                    updateDateDisplay(currentDateObj);
                    loadTimelineData();
                }
            }
        });

        window.openDatePicker = function () {
            if (fpInstance) {
                fpInstance.open();
            }
        };
    } catch (e) {
        console.warn("Flatpickr failed to initialize:", e);
    }
}

function updateDateDisplay(dateObj) {
    if (!currentDateDisplay) return;
    const d = dateObj.getDate().toString().padStart(2, '0');
    const m = (dateObj.getMonth() + 1).toString().padStart(2, '0');
    const y = dateObj.getFullYear();
    currentDateDisplay.textContent = `${d}/${m}/${y}`;
}

window.changeDate = function (days) {
    currentDateObj.setDate(currentDateObj.getDate() + days);
    currentDate = currentDateObj.toISOString().split('T')[0];
    updateDateDisplay(currentDateObj);
    if (fpInstance) {
        fpInstance.setDate(currentDateObj);
    }
    loadTimelineData();
};

async function loadTimelineData() {
    if (!currentFacilitySportId || !timelineGrid) return;

    if (loadingOverlay) loadingOverlay.classList.remove('hidden');

    try {
        const res = await fetch(`/api/owner/booking/timeline?facilitySportId=${currentFacilitySportId}&date=${currentDate}`);
        if (!res.ok) throw new Error("Failed to fetch data");

        const data = await res.json();
        renderGrid(data);
    } catch (error) {
        console.error("Error loading timeline:", error);
        timelineGrid.innerHTML = `<div class="p-8 text-center text-slate-500 col-span-full">Lỗi khi tải dữ liệu. Vui lòng thử lại.</div>`;
    } finally {
        if (loadingOverlay) loadingOverlay.classList.add('hidden');
    }
}

function renderGrid(data) {
    if (!timelineGrid) return;
    timelineGrid.innerHTML = '';
    const courts = data.courts || [];
    if (courts.length === 0) {
        timelineGrid.innerHTML = `<div class="p-8 text-center text-slate-500 col-span-full">Không có sân nào hoạt động.</div>`;
        return;
    }

    const numSlots = courts[0].slots.length;
    timelineGrid.style.setProperty('--total-slots', numSlots);

    // Render Header Row
    const headerFrag = document.createDocumentFragment();
    const cornerCell = document.createElement('div');
    cornerCell.className = 'timeline-header court-name text-xs uppercase tracking-wider text-slate-400 font-bold border-b border-slate-200';
    cornerCell.textContent = 'COURT / TIME';
    headerFrag.appendChild(cornerCell);

    courts[0].slots.forEach(slot => {
        const timeCell = document.createElement('div');
        timeCell.className = 'timeline-header timeline-cell font-medium text-xs';
        timeCell.textContent = slot.startTime;
        headerFrag.appendChild(timeCell);
    });
    timelineGrid.appendChild(headerFrag);

    // Render Courts and Slots
    courts.forEach(court => {
        const courtFrag = document.createDocumentFragment();

        const nameCell = document.createElement('div');
        nameCell.className = 'timeline-cell court-name text-xs text-slate-700';
        nameCell.textContent = court.courtName;
        courtFrag.appendChild(nameCell);

        court.slots.forEach(slot => {
            const slotCell = document.createElement('div');
            slotCell.className = 'timeline-cell';

            if (slot.status === 'BOOKED') {
                slotCell.classList.add('slot-booked');
                const bName = slot.bookerName || 'Guest';
                const bPhone = slot.bookerPhone || 'N/A';
                slotCell.innerHTML = `
                    <span class="font-bold text-xs uppercase tracking-wider">Booked</span>
                    <div class="booking-tooltip">
                        <div class="font-bold text-slate-800 mb-1 text-sm">${bName}</div>
                        <div class="text-slate-500 flex items-center gap-1.5 font-medium">
                            <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"/></svg>
                            ${bPhone}
                        </div>
                    </div>
                `;
            } else if (slot.status === 'HOLD') {
                slotCell.classList.add('slot-hold');
                const bName = slot.bookerName || 'Guest';
                const bPhone = slot.bookerPhone || 'N/A';
                slotCell.innerHTML = `
                    <span class="font-bold text-orange-900 text-xs uppercase tracking-wider">Pending</span>
                    <div class="booking-tooltip">
                        <div class="font-bold text-slate-800 mb-1 text-sm">${bName}</div>
                        <div class="text-slate-500 flex items-center gap-1.5 font-medium">
                            <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"/></svg>
                            ${bPhone}
                        </div>
                    </div>
                `;
            } else {
                slotCell.classList.add('slot-available', 'cursor-pointer');
                const isSel = selectedSlots.some(s => s.courtId === court.courtId && s.startTime === slot.startTime);
                if (isSel) {
                    slotCell.classList.add('slot-selected');
                    slotCell.innerHTML = `<span class="font-bold text-xs text-blue-700">Selected</span>`;
                }
                slotCell.onclick = () => toggleSlotSelect(court.courtId, court.courtName, slot, slotCell);
            }

            courtFrag.appendChild(slotCell);
        });
        timelineGrid.appendChild(courtFrag);
    });
}

// --- ON-SITE BOOKING LOGIC ---
function toggleSlotSelect(courtId, courtName, slot, cellElement) {
    const index = selectedSlots.findIndex(s => s.courtId === courtId && s.startTime === slot.startTime);
    if (index >= 0) {
        selectedSlots.splice(index, 1);
        cellElement.classList.remove('slot-selected');
        cellElement.innerHTML = '';
    } else {
        selectedSlots.push({
            courtId: courtId,
            courtName: courtName,
            startTime: slot.startTime,
            endTime: slot.endTime,
            price: slot.price || 0
        });
        cellElement.classList.add('slot-selected');
        cellElement.innerHTML = `<span class="font-bold text-xs text-blue-700">Selected</span>`;
    }
    updateSelectionSummaryBar();
}

window.clearSlotSelection = function () {
    selectedSlots = [];
    document.querySelectorAll('.slot-selected').forEach(cell => {
        cell.classList.remove('slot-selected');
        cell.innerHTML = '';
    });
    updateSelectionSummaryBar();
};

function updateSelectionSummaryBar() {
    const bar = document.getElementById('selectionSummaryBar');
    if (!bar) return;

    if (selectedSlots.length === 0) {
        bar.classList.add('hidden');
        return;
    }

    bar.classList.remove('hidden');
    document.getElementById('selectedSlotsCount').textContent = selectedSlots.length;

    const totalCourtFee = selectedSlots.reduce((sum, s) => sum + Number(s.price), 0);
    document.getElementById('selectedSlotsCourtText').textContent = `Đã chọn ${selectedSlots.length} ca`;
    document.getElementById('selectedSlotsPriceText').textContent = `Tổng tiền: ${formatVND(totalCourtFee)}`;
}

function formatVND(amount) {
    return new Intl.NumberFormat('vi-VN').format(amount) + ' VNĐ';
}

window.openOnSiteBookingModal = async function () {
    if (selectedSlots.length === 0) {
        alert("Vui lòng click chọn ít nhất 1 ca trống trên lịch để đặt sân!");
        return;
    }

    const modal = document.getElementById('onSiteBookingModal');
    if (modal) modal.classList.remove('hidden');

    const listEl = document.getElementById('modalSelectedSlotsList');
    let courtFeeTotal = 0;
    if (listEl) {
        listEl.innerHTML = selectedSlots.map(s => {
            courtFeeTotal += Number(s.price);
            return `<div class="flex justify-between items-center py-1 border-b border-blue-100/50 last:border-none">
                <span>• ${s.courtName} (${s.startTime} - ${s.endTime})</span>
                <span class="font-bold text-blue-900">${formatVND(s.price)}</span>
            </div>`;
        }).join('');
    }

    const feeDisp = document.getElementById('modalCourtFeeDisplay');
    if (feeDisp) feeDisp.textContent = formatVND(courtFeeTotal);

    loadFacilityProducts();
    updateModalTotalFee();
};

window.closeOnSiteBookingModal = function () {
    const modal = document.getElementById('onSiteBookingModal');
    if (modal) modal.classList.add('hidden');
};

async function loadFacilityProducts() {
    const container = document.getElementById('modalProductsContainer');
    if (!container || !currentFacilityId) return;

    try {
        const res = await fetch(`/api/owner/booking/products?facilityId=${currentFacilityId}`);
        if (!res.ok) throw new Error("Failed to fetch products");
        facilityProducts = await res.json();

        if (facilityProducts.length === 0) {
            container.innerHTML = `<div class="text-xs text-slate-400 italic p-3 text-center col-span-full">Không có dịch vụ thêm nào tại cơ sở này.</div>`;
            return;
        }

        container.innerHTML = facilityProducts.map(p => {
            const qty = selectedProductsMap[p.productId] || 0;
            return `<div class="p-3 bg-white rounded-xl border border-slate-200 flex items-center justify-between shadow-xs">
                <div>
                    <div class="font-bold text-xs text-slate-800">${p.productName}</div>
                    <div class="text-[11px] text-emerald-600 font-semibold">${formatVND(p.price)} ${p.unit ? '/ ' + p.unit : ''}</div>
                </div>
                <div class="flex items-center gap-2">
                    <button onclick="changeProductQty(${p.productId}, -1)" class="w-6 h-6 rounded-md bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-xs flex items-center justify-center transition-colors">-</button>
                    <span id="prodQty-${p.productId}" class="text-xs font-bold text-slate-800 w-4 text-center">${qty}</span>
                    <button onclick="changeProductQty(${p.productId}, 1)" class="w-6 h-6 rounded-md bg-blue-100 hover:bg-blue-200 text-blue-700 font-bold text-xs flex items-center justify-center transition-colors">+</button>
                </div>
            </div>`;
        }).join('');
    } catch (e) {
        console.error(e);
        container.innerHTML = `<div class="text-xs text-slate-400 italic p-3 text-center col-span-full">Không thể tải dịch vụ.</div>`;
    }
}

window.changeProductQty = function (productId, delta) {
    const currentQty = selectedProductsMap[productId] || 0;
    const newQty = Math.max(0, currentQty + delta);
    selectedProductsMap[productId] = newQty;

    const qtyEl = document.getElementById(`prodQty-${productId}`);
    if (qtyEl) qtyEl.textContent = newQty;

    updateModalTotalFee();
};

function updateModalTotalFee() {
    const courtFee = selectedSlots.reduce((sum, s) => sum + Number(s.price), 0);
    let productFee = 0;
    facilityProducts.forEach(p => {
        const qty = selectedProductsMap[p.productId] || 0;
        productFee += (Number(p.price) * qty);
    });

    const grandTotal = courtFee + productFee;
    const totalDisp = document.getElementById('modalTotalFeeDisplay');
    if (totalDisp) totalDisp.textContent = formatVND(grandTotal);
}

window.updatePaymentMethodStyle = function () {
    const isCash = document.querySelector('input[name="modalPaymentMethod"]:checked').value === 'CASH';
    const labelCash = document.getElementById('labelPaymentCash');
    const labelVnpay = document.getElementById('labelPaymentVnpay');

    if (isCash) {
        if (labelCash) labelCash.className = "flex items-center gap-3 p-3 border rounded-xl cursor-pointer bg-slate-50 hover:bg-white transition-all border-emerald-500 ring-2 ring-emerald-500/20";
        if (labelVnpay) labelVnpay.className = "flex items-center gap-3 p-3 border border-slate-200 rounded-xl cursor-pointer bg-slate-50 hover:bg-white transition-all";
    } else {
        if (labelCash) labelCash.className = "flex items-center gap-3 p-3 border border-slate-200 rounded-xl cursor-pointer bg-slate-50 hover:bg-white transition-all";
        if (labelVnpay) labelVnpay.className = "flex items-center gap-3 p-3 border rounded-xl cursor-pointer bg-slate-50 hover:bg-white transition-all border-blue-500 ring-2 ring-blue-500/20";
    }
};

window.submitOnSiteBooking = async function () {
    const customerName = document.getElementById('modalCustomerName').value.trim();
    const customerPhone = document.getElementById('modalCustomerPhone').value.trim();
    const customerEmail = document.getElementById('modalCustomerEmail').value.trim();
    const note = document.getElementById('modalBookingNote').value.trim();
    const paymentMethod = document.querySelector('input[name="modalPaymentMethod"]:checked').value;

    if (!customerName) {
        alert("Vui lòng nhập Họ và tên khách hàng!");
        document.getElementById('modalCustomerName').focus();
        return;
    }
    if (!customerPhone) {
        alert("Vui lòng nhập Số điện thoại khách hàng!");
        document.getElementById('modalCustomerPhone').focus();
        return;
    }
    if (selectedSlots.length === 0) {
        alert("Vui lòng chọn ca đặt!");
        return;
    }

    const servicesPayload = [];
    facilityProducts.forEach(p => {
        const qty = selectedProductsMap[p.productId] || 0;
        if (qty > 0) {
            servicesPayload.push({
                productId: p.productId,
                quantity: qty,
                price: p.price
            });
        }
    });

    const payload = {
        facilityId: currentFacilityId,
        facilitySportId: currentFacilitySportId,
        bookingDate: currentDate,
        customerName: customerName,
        customerPhone: customerPhone,
        customerEmail: customerEmail,
        note: note,
        paymentMethod: paymentMethod,
        slots: selectedSlots,
        services: servicesPayload
    };

    const btnSubmit = document.getElementById('btnSubmitOnSiteBooking');
    if (btnSubmit) {
        btnSubmit.disabled = true;
        btnSubmit.innerHTML = `<div class="animate-spin rounded-full h-4 w-4 border-b-2 border-slate-950"></div> <span>Đang xử lý...</span>`;
    }

    try {
        const res = await fetch('/api/owner/booking/create-onsite', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const result = await res.json();
        if (res.ok && result.success) {
            alert("🎉 Đặt sân tại chỗ thành công!");
            window.closeOnSiteBookingModal();
            window.clearSlotSelection();
            selectedProductsMap = {};
            document.getElementById('modalCustomerName').value = '';
            document.getElementById('modalCustomerPhone').value = '';
            document.getElementById('modalCustomerEmail').value = '';
            document.getElementById('modalBookingNote').value = '';
            loadTimelineData();
        } else {
            alert("Lỗi khi đặt sân: " + (result.message || "Không thể hoàn tất giao dịch."));
        }
    } catch (e) {
        console.error("On-site booking error:", e);
        alert("Đã xảy ra lỗi kết nối. Vui lòng thử lại.");
    } finally {
        if (btnSubmit) {
            btnSubmit.disabled = false;
            btnSubmit.innerHTML = `<span>Xác Nhận Đặt Sân</span><svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"/></svg>`;
        }
    }
};
