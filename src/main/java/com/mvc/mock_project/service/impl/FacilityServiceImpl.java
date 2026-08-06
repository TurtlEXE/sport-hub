package com.mvc.mock_project.service.impl;

import com.mvc.mock_project.dto.response.VenueCardDTO;
import com.mvc.mock_project.dto.response.VenueDetailDTO;
import com.mvc.mock_project.dto.response.ProductDTO;
import com.mvc.mock_project.dto.response.PriceRuleDTO;
import com.mvc.mock_project.entities.Facility;
import com.mvc.mock_project.entities.FacilityImage;
import com.mvc.mock_project.entities.FacilityPriceRule;
import com.mvc.mock_project.entities.FacilitySport;
import com.mvc.mock_project.entities.Product;
import com.mvc.mock_project.entities.ProductCategory;
import com.mvc.mock_project.entities.Sport;
import com.mvc.mock_project.repository.FacilityImageRepository;
import com.mvc.mock_project.repository.FacilityPriceRuleRepository;
import com.mvc.mock_project.repository.FacilityRepository;
import com.mvc.mock_project.repository.FacilitySportRepository;
import com.mvc.mock_project.repository.ProductCategoryRepository;
import com.mvc.mock_project.repository.ProductRepository;
import com.mvc.mock_project.repository.SportRepository;
import com.mvc.mock_project.repository.ReviewRepository;
import com.mvc.mock_project.service.FacilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacilityServiceImpl implements FacilityService {

    private final FacilityRepository facilityRepository;
    private final SportRepository sportRepository;
    private final FacilityImageRepository facilityImageRepository;
    private final FacilityPriceRuleRepository facilityPriceRuleRepository;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ReviewRepository reviewRepository;
    private final com.mvc.mock_project.repository.CustomerFavoriteFacilityRepository favoriteRepository;

    @Override
    public List<Sport> getAllActiveSports() {
        return sportRepository.findByIsActiveTrue();
    }

    @Override
    public List<ProductCategory> getAllActiveProductCategories() {
        return productCategoryRepository.findByIsActiveTrue();
    }

    @Override
    public List<VenueCardDTO> getFilteredFacilities(String keyword, String sportCode, Double minPrice, Double maxPrice, String province, List<Integer> categoryIds) {
        List<VenueCardDTO> allVenues = getAllActiveVenues();

        List<Integer> favoriteIds = null;
        if (Boolean.TRUE.equals(onlyFavorites) && accountId != null) {
            favoriteIds = favoriteRepository.findFacilityIdsByAccountId(accountId);
        }
        final List<Integer> finalFavoriteIds = favoriteIds;

        return allVenues.stream()
                .filter(v -> keyword == null || keyword.trim().isEmpty() || v.getName().toLowerCase().contains(keyword.toLowerCase()) || (v.getAddress() != null && v.getAddress().toLowerCase().contains(keyword.toLowerCase())))
                .filter(v -> sportCode == null || sportCode.trim().isEmpty() || (v.getSports() != null && v.getSports().contains(sportCode)))
                .filter(v -> minPrice == null || v.getMinPricePerHour() >= minPrice)
                .filter(v -> {
                    if (Boolean.TRUE.equals(onlyFavorites) && accountId != null) {
                        return finalFavoriteIds != null && finalFavoriteIds.contains(v.getFacilityId());
                    }
                    return true;
                })
                .filter(v -> keyword == null || keyword.trim().isEmpty()
                        || v.getName().toLowerCase().contains(keyword.toLowerCase())
                        || v.getAddress().toLowerCase().contains(keyword.toLowerCase()))
                .filter(v -> sportCode == null || sportCode.trim().isEmpty() || v.getSports().contains(sportCode))
                .filter(v -> maxPrice == null || v.getMinPricePerHour() <= maxPrice)
                .filter(v -> province == null || province.trim().isEmpty() || (v.getProvince() != null && v.getProvince().equalsIgnoreCase(province.trim())))
                .filter(v -> {
                    if (categoryIds == null || categoryIds.isEmpty()) return true;
                    if (categoryIds == null || categoryIds.isEmpty())
                        return true;
                    // For each required category, check if the venue has it
                    // The venue's amenities (List<String>) now contains the names of the categories
                    // but we need to check IDs. Since the mock project requires in-memory
                    // filtering,
                    // we'll fetch the venue's products and check their category IDs.
                    List<Product> products = productRepository.findByFacility_IdAndIsActiveTrue(v.getFacilityId());
                    List<Integer> venueCategoryIds = products.stream()
                            .map(p -> p.getCategory().getId())
                            .distinct()
                            .collect(Collectors.toList());
                    return venueCategoryIds.containsAll(categoryIds);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getDistinctProvinces() {
        return facilityRepository.findByIsActiveTrueAndApprovalStatus(com.mvc.mock_project.entities.enums.ApprovalStatus.APPROVED)
                .stream()
                .map(f -> f.getProvince())
                .filter(p -> p != null && !p.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public List<VenueCardDTO> getAllActiveVenues() {
        List<Facility> facilities = facilityRepository
                .findByIsActiveTrueAndApprovalStatus(com.mvc.mock_project.entities.enums.ApprovalStatus.APPROVED);
        List<VenueCardDTO> venueCards = new ArrayList<>();
        Random random = new Random();

        for (Facility facility : facilities) {
            // Get Thumbnail
            FacilityImage thumbnail = facilityImageRepository.findFirstByFacilityIdAndIsThumbnailTrue(facility.getId());
            String imageUrl = (thumbnail != null) ? thumbnail.getImagePath()
                    : "https://via.placeholder.com/400x250?text=No+Image";

            // Get Sports
            List<String> sports = new ArrayList<>();
            List<String> sportNames = new ArrayList<>();
            Double minPrice = Double.MAX_VALUE;

            if (facility.getFacilitySports() != null) {
                for (FacilitySport fs : facility.getFacilitySports()) {
                    if (Boolean.TRUE.equals(fs.getIsActive())) {
                        sports.add(fs.getSport().getSportCode());
                        sportNames.add(fs.getSport().getSportName());
                        // Find min price for this sport
                        List<FacilityPriceRule> rules = facilityPriceRuleRepository
                                .findByFacilitySportIdAndIsActiveTrue(fs.getId());
                        for (FacilityPriceRule rule : rules) {
                            if (rule.getPricePerSlot() != null && rule.getPricePerSlot().doubleValue() < minPrice) {
                                minPrice = rule.getPricePerSlot().doubleValue();
                            }
                        }
                    }
                }
            }

            if (minPrice == Double.MAX_VALUE) {
                minPrice = 0.0;
            }

            // Default rating and review count since it's not implemented yet
            double rating = 0.0;
            int reviewCount = 0;

            // Real amenities from products
            List<Product> products = productRepository.findByFacility_IdAndIsActiveTrue(facility.getId());
            List<String> amenities = products.stream()
                    .map(p -> p.getCategory().getCategoryName())
                    .distinct()
                    .collect(Collectors.toList());

            // Format opening hours
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            String openingHours = "";
            if (facility.getOpenTime() != null && facility.getCloseTime() != null) {
                openingHours = facility.getOpenTime().format(formatter) + " - "
                        + facility.getCloseTime().format(formatter);
            }

            VenueCardDTO dto = VenueCardDTO.builder()
                    .facilityId(facility.getId())
                    .name(facility.getName())
                    .address(facility.getAddress())
                    .province(facility.getProvince())
                    .imageUrl(imageUrl)
                    .sports(sports)
                    .sportNames(sportNames)
                    .rating(Math.round(rating * 10.0) / 10.0)
                    .reviewCount(reviewCount)
                    .openingHours(openingHours)
                    .minPricePerHour(minPrice)
                    .amenities(amenities)
                    .latitude(facility.getLatitude() != null ? facility.getLatitude().doubleValue() : null)
                    .longitude(facility.getLongitude() != null ? facility.getLongitude().doubleValue() : null)
                    .description(facility.getDescription())
                    .build();
            venueCards.add(dto);
        }

        return venueCards;
    }

    @Override
    public VenueCardDTO getVenueById(Integer id) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facility not found"));
        Random random = new Random();

        // Get Thumbnail
        FacilityImage thumbnail = facilityImageRepository.findFirstByFacilityIdAndIsThumbnailTrue(facility.getId());
        String imageUrl = (thumbnail != null) ? thumbnail.getImagePath()
                : "https://via.placeholder.com/400x250?text=No+Image";

        // Get Sports
        List<String> sports = new ArrayList<>();
        List<String> sportNames = new ArrayList<>();
        Double minPrice = Double.MAX_VALUE;

        if (facility.getFacilitySports() != null) {
            for (FacilitySport fs : facility.getFacilitySports()) {
                if (Boolean.TRUE.equals(fs.getIsActive())) {
                    sports.add(fs.getSport().getSportCode());
                    sportNames.add(fs.getSport().getSportName());
                    List<FacilityPriceRule> rules = facilityPriceRuleRepository
                            .findByFacilitySportIdAndIsActiveTrue(fs.getId());
                    for (FacilityPriceRule rule : rules) {
                        if (rule.getPricePerSlot() != null && rule.getPricePerSlot().doubleValue() < minPrice) {
                            minPrice = rule.getPricePerSlot().doubleValue();
                        }
                    }
                }
            }
        }

        if (minPrice == Double.MAX_VALUE) {
            minPrice = 0.0;
        }

        double rating = 0.0;
        int reviewCount = 0;

        List<Product> products = productRepository.findByFacility_IdAndIsActiveTrue(facility.getId());
        List<String> amenities = products.stream()
                .map(p -> p.getCategory().getCategoryName())
                .distinct()
                .collect(Collectors.toList());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String openingHours = "";
        if (facility.getOpenTime() != null && facility.getCloseTime() != null) {
            openingHours = facility.getOpenTime().format(formatter) + " - " + facility.getCloseTime().format(formatter);
        }

        return VenueCardDTO.builder()
                .facilityId(facility.getId())
                .name(facility.getName())
                .address(facility.getAddress())
                .imageUrl(imageUrl)
                .sports(sports)
                .sportNames(sportNames)
                .rating(Math.round(rating * 10.0) / 10.0)
                .reviewCount(reviewCount)
                .openingHours(openingHours)
                .minPricePerHour(minPrice)
                .amenities(amenities)
                .latitude(facility.getLatitude() != null ? facility.getLatitude().doubleValue() : null)
                .longitude(facility.getLongitude() != null ? facility.getLongitude().doubleValue() : null)
                .description(facility.getDescription())
                .ownerName(facility.getOwner() != null ? facility.getOwner().getFullName() : "SportHub Admin")
                .contactPhone(facility.getOwner() != null ? facility.getOwner().getPhone() : null)
                .build();
    }

    @Override
    public VenueDetailDTO getVenueDetailById(Integer id) {
        VenueCardDTO base = getVenueById(id);
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facility not found"));

        // Fetch products
        List<Product> products = productRepository.findByFacility_IdAndIsActiveTrue(id);
        Map<String, List<ProductDTO>> groupedServices = products.stream().map(p -> ProductDTO.builder()
                .id(p.getId())
                .productName(p.getProductName())
                .price(p.getPrice())
                .rentalUnit(p.getRentalUnit())
                .categoryName(p.getCategory() != null ? p.getCategory().getCategoryName() : "Dịch vụ khác")
                .build()).collect(Collectors.groupingBy(ProductDTO::getCategoryName));

        // Fetch price rules
        List<FacilityPriceRule> allRules = new ArrayList<>();
        Integer slotDuration = 60; // Default
        if (facility.getFacilitySports() != null) {
            for (FacilitySport fs : facility.getFacilitySports()) {
                if (Boolean.TRUE.equals(fs.getIsActive())) {
                    if (fs.getMinDurationMinutes() != null) {
                        slotDuration = fs.getMinDurationMinutes();
                    }
                    allRules.addAll(facilityPriceRuleRepository.findByFacilitySportIdAndIsActiveTrue(fs.getId()));
                }
            }
        }

        Map<String, List<PriceRuleDTO>> groupedPriceRules = allRules.stream().map(r -> {
            String sportName = (r.getFacilitySport() != null && r.getFacilitySport().getSport() != null)
                    ? r.getFacilitySport().getSport().getSportName()
                    : "General";
            return PriceRuleDTO.builder()
                    .startTime(r.getStartTime())
                    .endTime(r.getEndTime())
                    .pricePerSlot(r.getPricePerSlot())
                    .dayType(r.getDayType() != null ? r.getDayType().name() : "ALL")
                    .sportName(sportName)
                    .build();
        }).collect(Collectors.groupingBy(PriceRuleDTO::getSportName));

        List<String> galleryImages = new ArrayList<>();
        if (facility.getImages() != null) {
            for (com.mvc.mock_project.entities.FacilityImage img : facility.getImages()) {
                if (!Boolean.TRUE.equals(img.getIsThumbnail()) && img.getImagePath() != null) {
                    galleryImages.add(img.getImagePath());
                }
            }
        }

        List<com.mvc.mock_project.entities.Review> reviewEntities = reviewRepository
                .findByFacilityIdOrderByCreatedAtDesc(id);
        List<com.mvc.mock_project.dto.response.ReviewDTO> reviewDTOs = reviewEntities.stream()
                .map(r -> com.mvc.mock_project.dto.response.ReviewDTO.builder()
                        .id(r.getId())
                        .reviewerName(r.getAccount() != null ? r.getAccount().getFullName() : "Khách")
                        .rating(r.getRating())
                        .comment(r.getComment())
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return VenueDetailDTO.builder()
                .facilityId(base.getFacilityId())
                .name(base.getName())
                .address(base.getAddress())
                .imageUrl(base.getImageUrl())
                .sports(base.getSports())
                .sportNames(base.getSportNames())
                .rating(base.getRating())
                .reviewCount(base.getReviewCount())
                .openingHours(base.getOpeningHours())
                .minPricePerHour(base.getMinPricePerHour())
                .amenities(base.getAmenities())
                .latitude(base.getLatitude())
                .longitude(base.getLongitude())
                .description(base.getDescription())
                .ownerName(base.getOwnerName())
                .contactPhone(base.getContactPhone())
                .groupedServices(groupedServices)
                .groupedPriceRules(groupedPriceRules)
                .slotDurationMinutes(slotDuration)
                .galleryImages(galleryImages)
                .reviews(reviewDTOs)
                .build();
    }
}
