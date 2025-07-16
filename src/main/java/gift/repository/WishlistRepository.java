package gift.repository;


import gift.entity.Member;
import gift.entity.Product;
import gift.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {


    List<Wishlist> findByMember(Member member);

    Optional<Wishlist> findByMemberAndProduct(Member member, Product product);

    void deleteByMemberAndProduct(Member member, Product product);
}

