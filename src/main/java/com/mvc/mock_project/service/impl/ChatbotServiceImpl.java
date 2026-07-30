package com.mvc.mock_project.service.impl;

import com.mvc.mock_project.entities.BookingSlot;
import com.mvc.mock_project.entities.Court;
import com.mvc.mock_project.entities.Facility;
import com.mvc.mock_project.entities.Voucher;
import com.mvc.mock_project.repository.BookingSlotRepository;
import com.mvc.mock_project.repository.CourtRepository;
import com.mvc.mock_project.repository.FacilityRepository;
import com.mvc.mock_project.repository.VoucherRepository;
import com.mvc.mock_project.service.ChatbotService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatbotServiceImpl implements ChatbotService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final FacilityRepository facilityRepository;
    private final CourtRepository courtRepository;
    private final BookingSlotRepository bookingSlotRepository;
    private final VoucherRepository voucherRepository;

    @Value("${ai.provider:gemini}")
    private String aiProvider;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-3.1-flash-lite}")
    private String geminiModel;

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String openaiModel;

    public ChatbotServiceImpl(FacilityRepository facilityRepository,
                              CourtRepository courtRepository,
                              BookingSlotRepository bookingSlotRepository,
                              VoucherRepository voucherRepository) {
        this.facilityRepository = facilityRepository;
        this.courtRepository = courtRepository;
        this.bookingSlotRepository = bookingSlotRepository;
        this.voucherRepository = voucherRepository;
    }

    @Override
    public String askQuestion(String userMessage, String language) {
        String systemPrompt = buildSystemContext(language);

        if ("gemini".equalsIgnoreCase(aiProvider) || (!geminiApiKey.isEmpty() && !"openai".equalsIgnoreCase(aiProvider))) {
            return askGemini(systemPrompt, userMessage);
        } else {
            return askOpenAI(systemPrompt, userMessage);
        }
    }

    private String askGemini(String systemPrompt, String userMessage) {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + geminiModel + ":generateContent?key=" + geminiApiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> systemInstruction = Map.of(
                    "parts", List.of(Map.of("text", systemPrompt))
            );

            Map<String, Object> content = Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", userMessage))
            );

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("systemInstruction", systemInstruction);
            requestBody.put("contents", List.of(content));
            requestBody.put("generationConfig", Map.of("temperature", 0.7));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(url, entity, Map.class);

            Map response = responseEntity.getBody();
            if (response != null && response.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> contentMap = (Map<String, Object>) candidate.get("content");
                    if (contentMap != null && contentMap.containsKey("parts")) {
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
                        if (!parts.isEmpty()) {
                            return (String) parts.get(0).get("text");
                        }
                    }
                }
            }
            return "Xin lỗi, hiện tại tôi không nhận được phản hồi từ Gemini AI.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Xin lỗi, đã xảy ra lỗi kết nối với Gemini AI: " + e.getMessage();
        }
    }

    private String askOpenAI(String systemPrompt, String userMessage) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userMessage));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", openaiModel);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(
                    "https://api.openai.com/v1/chat/completions",
                    entity,
                    Map.class
            );

            Map response = responseEntity.getBody();
            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
            return "Xin lỗi, hiện tại tôi không thể xử lý câu hỏi của bạn. Vui lòng thử lại sau.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Xin lỗi, đã xảy ra lỗi kết nối với OpenAI: " + e.getMessage();
        }
    }

    private String buildSystemContext(String language) {
        StringBuilder context = new StringBuilder();
        context.append("Bạn là trợ lý ảo AI thông minh của hệ thống đặt sân thể thao Sport Hub.\n");
        context.append("Nhiệm vụ của bạn là tư vấn, giải đáp thắc mắc cho khách hàng dựa trên dữ liệu thực tế dưới đây.\n");
        context.append("CHỈ THỊ BẮT BUỘC (MANDATORY DIRECTIVES):\n");
        context.append("1. Khi khách hàng hỏi về các chủ đề chung (ví dụ: sân bóng, giá thuê, địa chỉ), câu trả lời của bạn phải thật NGẮN GỌN, TRỰC TIẾP và RÕ RÀNG.\n");
        context.append("2. LUÔN LUÔN gợi ý thêm các tùy chọn liên quan (như chương trình khuyến mãi, mã giảm giá đang hoạt động, hoặc các tiện ích của sân) ở cuối câu trả lời một cách tự nhiên để thu hút khách hàng.\n");
        String langName = "Vietnamese";
        if ("en".equalsIgnoreCase(language)) langName = "English";
        else if ("mm".equalsIgnoreCase(language) || "my".equalsIgnoreCase(language)) langName = "Myanmar (Burmese)";
        
        context.append("3. Ngôn ngữ hiện tại của trang web là: ").append(langName).append(".\n");
        context.append("4. RẤT QUAN TRỌNG: Nếu khách hàng chỉ nói những câu ngắn ngọn (như 'hi', 'hello', 'chào'), hãy chào lại bằng ngôn ngữ hiện tại của trang web. TUY NHIÊN, nếu khách hàng hỏi bằng ngôn ngữ nào, BẠN BẮT BUỘC PHẢI TRẢ LỜI BẰNG NGÔN NGỮ ĐÓ (ví dụ khách hỏi Tiếng Anh thì trả lời Tiếng Anh).\n");
        context.append("5. KHÔNG SỬ DỤNG markdown in đậm (như **chữ**) trong câu trả lời vì trình duyệt không hiển thị được. Hãy viết text bình thường.\n");
        context.append("Thời gian hệ thống hiện tại là: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n\n");
        // 1. Facilities and Courts
        List<Facility> facilities = facilityRepository.findByIsActiveTrue();
        List<Court> allCourts = courtRepository.findAll().stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                .collect(Collectors.toList());

        context.append("--- DANH SÁCH CƠ SỞ THỂ THAO VÀ SÂN ---\n");
        for (Facility f : facilities) {
            context.append(String.format("Cơ sở: %s | Địa chỉ: %s | Giờ mở cửa: %s - %s\n",
                    f.getName(), f.getAddress(), f.getOpenTime(), f.getCloseTime()));
            List<Court> courtsInFacility = allCourts.stream()
                    .filter(c -> c.getFacilitySport() != null && c.getFacilitySport().getFacility().getId().equals(f.getId()))
                    .collect(Collectors.toList());
            for (Court c : courtsInFacility) {
                String sportName = c.getFacilitySport().getSport() != null ? c.getFacilitySport().getSport().getSportName() : "Môn khác";
                context.append(String.format("   + Sân ID: %d | Tên: %s (%s)\n", c.getId(), c.getCourtName(), sportName));
            }
        }

        // 2. Booking availability today and tomorrow
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        List<Integer> courtIds = allCourts.stream().map(Court::getId).collect(Collectors.toList());

        if (!courtIds.isEmpty()) {
            List<BookingSlot> todaySlots = bookingSlotRepository.findActiveSlotsByCourtIdsAndDate(courtIds, today);
            List<BookingSlot> tomorrowSlots = bookingSlotRepository.findActiveSlotsByCourtIdsAndDate(courtIds, tomorrow);

            context.append("\n--- CÁC KHUNG GIỜ ĐÃ CÓ NGƯỜI ĐẶT (KHÔNG TRỐNG) HÔM NAY (").append(today).append(") ---\n");
            if (todaySlots.isEmpty()) {
                context.append("Hôm nay tất cả các sân đều trống suốt giờ mở cửa!\n");
            } else {
                for (BookingSlot slot : todaySlots) {
                    context.append(String.format("Sân: %s | Thời gian: %s - %s (Đã đặt)\n",
                            slot.getCourt().getCourtName(), slot.getStartTime(), slot.getEndTime()));
                }
            }

            context.append("\n--- CÁC KHUNG GIỜ ĐÃ CÓ NGƯỜI ĐẶT (KHÔNG TRỐNG) NGÀY MAI (").append(tomorrow).append(") ---\n");
            if (tomorrowSlots.isEmpty()) {
                context.append("Ngày mai tất cả các sân đều trống suốt giờ mở cửa!\n");
            } else {
                for (BookingSlot slot : tomorrowSlots) {
                    context.append(String.format("Sân: %s | Thời gian: %s - %s (Đã đặt)\n",
                            slot.getCourt().getCourtName(), slot.getStartTime(), slot.getEndTime()));
                }
            }
        }

        // 3. Active Vouchers
        List<Voucher> vouchers = voucherRepository.findAll().stream()
                .filter(v -> Boolean.TRUE.equals(v.getIsActive()))
                .collect(Collectors.toList());
        context.append("\n--- CÁC MÃ GIẢM GIÁ (VOUCHER) ĐANG HOẠT ĐỘNG ---\n");
        if (vouchers.isEmpty()) {
            context.append("Hiện tại không có mã giảm giá nào.\n");
        } else {
            for (Voucher v : vouchers) {
                context.append(String.format("Mã: %s | Giảm: %s | Mô tả: %s\n",
                        v.getCode(),
                        v.getDiscountType().name().equals("PERCENTAGE") ? v.getDiscountValue() + "%" : v.getDiscountValue() + " VNĐ",
                        v.getDescription()));
            }
        }

        context.append("\nLưu ý: Nếu khách hỏi về các sân trống, hãy đối chiếu giữa giờ mở cửa cơ sở và các khung giờ đã bị đặt để thông báo chính xác cho khách khung giờ nào còn trống. Nếu khách muốn đặt sân, hãy hướng dẫn họ vào trang danh sách sân để chọn khung giờ và thanh toán.\n");
        return context.toString();
    }
}
