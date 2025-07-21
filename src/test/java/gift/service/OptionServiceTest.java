package gift.service;

import gift.dto.OptionResponseDto;
import gift.entity.Option;
import gift.entity.Product;
import gift.repository.OptionRepository;
import gift.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.any;

class OptionServiceTest {

    private OptionService optionService;
    private OptionRepository optionRepository;
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        optionRepository = Mockito.mock(OptionRepository.class);
        productRepository = Mockito.mock(ProductRepository.class);
        optionService = new OptionService(optionRepository, productRepository);
    }

    @Test
    void getOptionsByProductId_Success() {
        Long productId = 1L;
        given(productRepository.existsById(productId)).willReturn(true);
        Product product = new Product(productId, "p", 100, "url");
        Option option = new Option(product, "opt1", 5);
        given(optionRepository.findByProductId(productId)).willReturn(List.of(option));

        List<OptionResponseDto> result = optionService.getOptionsByProductId(productId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("opt1");
    }

    @Test
    void getOptionsByProductId_ProductNotFound() {
        Long productId = 1L;
        given(productRepository.existsById(productId)).willReturn(false);

        assertThatThrownBy(() -> optionService.getOptionsByProductId(productId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("상품이 존재하지 않습니다.");
    }

    @Test
    void getOptionByIdAndProductId_Success() {
        Long productId = 1L, optionId = 2L;
        Product product = new Product(productId, "p", 100, "url");
        Option option = new Option(product, "opt", 10);
        given(optionRepository.findByIdAndProductId(optionId, productId)).willReturn(Optional.of(option));

        OptionResponseDto dto = optionService.getOptionByIdAndProductId(optionId, productId);

        assertThat(dto.id()).isEqualTo(optionId);
        assertThat(dto.quantity()).isEqualTo(10);
    }

    @Test
    void getOptionByIdAndProductId_NotFound() {
        given(optionRepository.findByIdAndProductId(2L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> optionService.getOptionByIdAndProductId(2L, 1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("옵션을 찾을 수 없습니다.");
    }

    @Test
    void addOption_Success() {
        Long productId = 1L;
        String name = "opt";
        int quantity = 5;
        Product product = new Product(productId, "p", 100, "url");
        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(optionRepository.existsByProductIdAndName(productId, name)).willReturn(false);

        optionService.addOption(productId, name, quantity);

        then(optionRepository).should().save(any(Option.class));
    }

    @Test
    void addOption_ProductNotFound() {
        given(productRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> optionService.addOption(1L, "opt", 5))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("상품이 존재하지 않습니다.");
    }

    @Test
    void addOption_DuplicateName() {
        Long productId = 1L;
        given(productRepository.findById(productId)).willReturn(Optional.of(new Product(productId, "p", 100, "url")));
        given(optionRepository.existsByProductIdAndName(productId, "opt")).willReturn(true);

        assertThatThrownBy(() -> optionService.addOption(productId, "opt", 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 옵션 이름입니다.");
    }

    @Test
    void subtractQuantity_Success() {
        Option option = new Option(new Product(1L, "p", 100, "url"), "opt", 10);
        given(optionRepository.findById(1L)).willReturn(Optional.of(option));

        optionService.subtractQuantity(1L, 3);

        assertThat(option.getQuantity()).isEqualTo(7);
    }

    @Test
    void subtractQuantity_OptionNotFound() {
        given(optionRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> optionService.subtractQuantity(1L, 1))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("옵션을 찾을 수 없습니다.");
    }

    @Test
    void subtractQuantity_InsufficientStock() {
        Option option = new Option(new Product(1L, "p", 100, "url"), "opt", 2);
        given(optionRepository.findById(1L)).willReturn(Optional.of(option));

        assertThatThrownBy(() -> optionService.subtractQuantity(1L, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("재고가 부족합니다.");
    }

    @Test
    void updateOption_Success() {
        Long productId = 1L, optionId = 2L;
        given(productRepository.existsById(productId)).willReturn(true);
        Product product = new Product(productId, "p", 100, "url");
        Option option = new Option(product, "old", 5);
        given(optionRepository.findByIdAndProductId(optionId, productId)).willReturn(Optional.of(option));
        given(optionRepository.existsByProductIdAndName(productId, "new")).willReturn(false);

        optionService.updateOption(productId, optionId, "new", 10);

        assertThat(option.getName()).isEqualTo("new");
        assertThat(option.getQuantity()).isEqualTo(10);
    }

    @Test
    void updateOption_ProductNotFound() {
        given(productRepository.existsById(1L)).willReturn(false);

        assertThatThrownBy(() -> optionService.updateOption(1L, 2L, "n", 5))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("상품이 존재하지 않습니다.");
    }

    @Test
    void updateOption_OptionNotFound() {
        given(productRepository.existsById(1L)).willReturn(true);
        given(optionRepository.findByIdAndProductId(2L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> optionService.updateOption(1L, 2L, "n", 5))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("옵션을 찾을 수 없습니다.");
    }

    @Test
    void updateOption_DuplicateName() {
        Long productId = 1L, optionId = 2L;
        given(productRepository.existsById(productId)).willReturn(true);
        Product product = new Product(productId, "p", 100, "url");
        Option option = new Option(product, "old", 5);
        given(optionRepository.findByIdAndProductId(optionId, productId)).willReturn(Optional.of(option));
        given(optionRepository.existsByProductIdAndName(productId, "old")).willReturn(true);

        assertThatThrownBy(() -> optionService.updateOption(productId, optionId, "old", 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 옵션 이름입니다.");
    }

    @Test
    void deleteOption_Success() {
        Long productId = 1L, optionId = 2L;
        given(productRepository.existsById(productId)).willReturn(true);
        Option option = new Option(new Product(productId, "p", 100, "url"), "opt", 5);
        given(optionRepository.findByIdAndProductId(optionId, productId)).willReturn(Optional.of(option));

        optionService.deleteOption(productId, optionId);

        then(optionRepository).should().deleteByIdAndProductId(optionId, productId);
    }

    @Test
    void deleteOption_ProductNotFound() {
        given(productRepository.existsById(1L)).willReturn(false);

        assertThatThrownBy(() -> optionService.deleteOption(1L, 2L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("상품이 존재하지 않습니다.");
    }

    @Test
    void deleteOption_OptionNotFound() {
        given(productRepository.existsById(1L)).willReturn(true);
        given(optionRepository.findByIdAndProductId(2L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> optionService.deleteOption(1L, 2L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("옵션을 찾을 수 없습니다.");
    }
}
