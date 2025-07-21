package gift.service;

import gift.dto.OptionResponseDto;
import gift.entity.Option;
import gift.entity.Product;
import gift.repository.OptionRepository;
import gift.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OptionService {

    private final OptionRepository optionRepository;
    private final ProductRepository productRepository;

    public OptionService(OptionRepository optionRepository, ProductRepository productRepository) {
        this.optionRepository = optionRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<OptionResponseDto> getOptionsByProductId(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new EntityNotFoundException("상품이 존재하지 않습니다.");
        }
        return optionRepository.findByProductId(productId).stream()
                .map(o -> new OptionResponseDto(o.getId(), o.getName(), o.getQuantity()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OptionResponseDto getOptionByIdAndProductId(Long optionId, Long productId) {
        Option o = optionRepository.findByIdAndProductId(optionId, productId)
                .orElseThrow(() -> new EntityNotFoundException("옵션을 찾을 수 없습니다."));
        return new OptionResponseDto(optionId, o.getName(), o.getQuantity());
    }

    @Transactional
    public void subtractQuantity(Long optionId, int quantity) {
        Option option = optionRepository.findById(optionId)
                .orElseThrow(() -> new EntityNotFoundException("옵션을 찾을 수 없습니다."));
        option.subtract(quantity);
    }

    @Transactional
    public void addOption(Long productId, String name, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품이 존재하지 않습니다."));
        if (optionRepository.existsByProductIdAndName(productId, name)) {
            throw new IllegalArgumentException("이미 존재하는 옵션 이름입니다.");
        }
        optionRepository.save(new Option(product, name, quantity));
    }

    @Transactional
    public void updateOption(Long productId, Long optionId, String name, int quantity) {
        if (!productRepository.existsById(productId)) {
            throw new EntityNotFoundException("상품이 존재하지 않습니다.");
        }
        Option option = optionRepository.findByIdAndProductId(optionId, productId)
                .orElseThrow(() -> new EntityNotFoundException("옵션을 찾을 수 없습니다."));
        if (optionRepository.existsByProductIdAndName(productId, name)) {
            throw new IllegalArgumentException("이미 존재하는 옵션 이름입니다.");
        }
        option.update(name, quantity);
    }


    @Transactional
    public void deleteOption(Long productId, Long optionId) {
        if (!productRepository.existsById(productId)) {
            throw new EntityNotFoundException("상품이 존재하지 않습니다.");
        }
        optionRepository.findByIdAndProductId(optionId, productId)
                .orElseThrow(() -> new EntityNotFoundException("옵션을 찾을 수 없습니다."));
        optionRepository.deleteByIdAndProductId(optionId, productId);
    }
}
