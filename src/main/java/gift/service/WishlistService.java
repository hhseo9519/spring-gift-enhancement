package gift.service;

import gift.dto.ProductResponseDto;
import gift.entity.Member;
import gift.entity.Product;
import gift.entity.Wishlist;
import gift.repository.WishlistRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductService productService;
    private final MemberService memberService;

    public WishlistService(WishlistRepository wishlistRepository,
            ProductService productService,
            MemberService memberService) {
        this.wishlistRepository = wishlistRepository;
        this.productService = productService;
        this.memberService = memberService;
    }

    public List<ProductResponseDto> getWishlist(Long memberId) {
        Member member = memberService.findById(memberId);
        List<Wishlist> list = wishlistRepository.findByMember(member);

        return list.stream()
                .map(Wishlist::getProduct)
                .filter(Objects::nonNull)

                .map(p -> new ProductResponseDto(
                        p.getId(),
                        p.getName(),
                        p.getPrice(),

                        p.getImageUrl()))
                .toList();
    }

    public void addToWishlist(Long memberId, Long productId) {
        Member member = memberService.findById(memberId);
        Product product = productService.findProductEntity(productId);

        wishlistRepository.findByMemberAndProduct(member, product)
                .ifPresentOrElse(
                        Wishlist::increaseQuantity,
                        () -> wishlistRepository.save(new Wishlist(member, product))
                );
    }

    public void removeFromWishlist(Long memberId, Long productId) {
        Member member = memberService.findById(memberId);
        Product product = productService.findProductEntity(productId);

        wishlistRepository.findByMemberAndProduct(member, product)
                .ifPresent(wishlistRepository::delete);
    }
}

