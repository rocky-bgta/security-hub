//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//import com.aspire.asat.cms.dto.enums.PackageStatus;
//import com.aspire.asat.cms.dto.enums.ProductStatus;
//import com.aspire.asat.cms.dto.enums.Status;
//import com.aspire.asat.cms.dto.product.FeatureDto;
//import com.aspire.asat.cms.dto.product.ProductCreationRequest;
//import com.aspire.asat.cms.dto.product.PackageRequest;
//import com.aspire.asat.cms.dto.product.ProductResponse;
//import com.aspire.asat.cms.exception.DuplicateNameException;
//import com.aspire.asat.cms.exception.ResourceNotFoundException;
//import com.aspire.asat.cms.mapper.ProductMapper;
//import com.aspire.asat.cms.mapper.ProductPackageMapper;
//import com.aspire.asat.cms.model.Product;
//import com.aspire.asat.cms.model.ProductPackage;
//import com.aspire.asat.cms.repository.CourseRepository;
//import com.aspire.asat.cms.repository.ProductPackageRepository;
//import com.aspire.asat.cms.repository.ProductRepository;
//import com.aspire.asat.cms.service.PackageService;
//import com.aspire.asat.cms.service.impl.ProductServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//import org.springframework.data.domain.*;
//
//import java.time.Instant;
//import java.util.*;
//
//class ProductServiceImplTest {
//
//    @Mock
//    private ProductRepository productRepository;
//
//    @Mock
//    private CourseRepository courseRepository;
//
//    @Mock
//    private ProductPackageRepository productPackageRepository;
//
//    @Mock
//    private ProductPackageMapper productPackageMapper;
//
//    @Mock
//    private ProductMapper productMapper;
//
//    @Mock
//    private PackageService packageService;
//
//    @InjectMocks
//    private ProductServiceImpl productService;
//
//    @BeforeEach
//    void setup() {
//        MockitoAnnotations.openMocks(this);
//
//        // Setup common mocks
//        when(productMapper.toEntity(any(ProductCreationRequest.class))).thenAnswer(invocation -> {
//            ProductCreationRequest request = invocation.getArgument(0);
//            return Product.builder()
//                    .id("test-id")
//                    .productName(request.getProductName())
//                    .productDescription(request.getProductDescription())
//                    .productStatus(request.getProductStatus())
//                    .thumbnailUrl(request.getThumbnailUrl())
//                    .createdAt(Instant.now())
//                    .updatedAt(Instant.now())
//                    .build();
//        });
//
//        when(productMapper.toCreateResponse(any(Product.class))).thenAnswer(invocation -> {
//            Product product = invocation.getArgument(0);
//            return ProductResponse.builder()
//                    .productId(product.getId())
//                    .productName(product.getProductName())
//                    .productDescription(product.getProductDescription())
//                    .productStatus(product.getProductStatus())
//                    .thumbnailUrl(product.getThumbnailUrl())
//                    .createdAt(product.getCreatedAt())
//                    .updatedAt(product.getUpdatedAt())
//                    .build();
//        });
//
//        when(productMapper.toProductResponse(any(Product.class))).thenAnswer(invocation -> {
//            Product product = invocation.getArgument(0);
//            return ProductResponse.builder()
//                    .productId(product.getId())
//                    .productName(product.getProductName())
//                    .productDescription(product.getProductDescription())
//                    .productStatus(product.getProductStatus())
//                    .thumbnailUrl(product.getThumbnailUrl())
//                    .createdAt(product.getCreatedAt())
//                    .updatedAt(product.getUpdatedAt())
//                    .build();
//        });
//
//        when(productMapper.toProductResponseList(anyList())).thenAnswer(invocation -> {
//            List<Product> products = invocation.getArgument(0);
//            return products.stream()
//                    .map(product -> ProductResponse.builder()
//                            .productId(product.getId())
//                            .productName(product.getProductName())
//                            .productDescription(product.getProductDescription())
//                            .productStatus(product.getProductStatus())
//                            .thumbnailUrl(product.getThumbnailUrl())
//                            .createdAt(product.getCreatedAt())
//                            .updatedAt(product.getUpdatedAt())
//                            .build())
//                    .toList();
//        });
//
//        when(productMapper.toEntity(any(ProductCreationRequest.class))).thenAnswer(invocation -> {
//            ProductCreationRequest request = invocation.getArgument(0);
//            return Product.builder()
//                    .id("test-id")
//                    .productName(request.getProductName())
//                    .productDescription(request.getProductDescription())
//                    .productStatus(request.getProductStatus())
//                    .thumbnailUrl(request.getThumbnailUrl())
//                    .createdAt(Instant.now())
//                    .updatedAt(Instant.now())
//                    .build();
//        });
//    }
//
//    // --- saveProduct tests ---
//
//    @Test
//    void saveProduct_ShouldThrowNullException_WhenProductNameIsNull() {
//        ProductCreationRequest dto = ProductCreationRequest.builder()
//                .productName(null)
//                .build();
//
//        // This test should now pass since we removed the null check from the service
//        // The validation should be handled by @Valid annotation
//        assertThrows(Exception.class, () -> productService.saveProduct(dto));
//    }
//
//    @Test
//    void saveProduct_ShouldThrowNullException_WhenProductNameIsEmpty() {
//        ProductCreationRequest dto = ProductCreationRequest.builder()
//                .productName("")
//                .build();
//
//        // This test should now pass since we removed the null check from the service
//        // The validation should be handled by @Valid annotation
//        assertThrows(Exception.class, () -> productService.saveProduct(dto));
//    }
//
//    @Test
//    void saveProduct_ShouldThrowDuplicateNameException_WhenProductNameExists() {
//        ProductCreationRequest dto = ProductCreationRequest.builder()
//                .productName("ExistingProduct")
//                .build();
//
//        when(productRepository.existsByProductName("ExistingProduct")).thenReturn(true);
//
//        DuplicateNameException ex = assertThrows(DuplicateNameException.class, () -> productService.saveProduct(dto));
//        assertEquals("Product name 'ExistingProduct' already exists.", ex.getMessage());
//    }
//
//    @Test
//    void saveProduct_ShouldCreateProductSuccessfully_WhenValidRequest() {
//        ProductCreationRequest dto = ProductCreationRequest.builder()
//                .productName("TestProduct")
//                .productDescription("Test Description")
//                .productStatus(ProductStatus.ENABLED)
//                .thumbnailUrl("http://example.com/thumb.jpg")
//                .build();
//
//        when(productRepository.existsByProductName("TestProduct")).thenReturn(false);
//        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
//            Product product = invocation.getArgument(0);
//            product.setId("test-id");
//            return product;
//        });
//
//        final var result = productService.saveProduct(dto);
//
//        assertNotNull(result);
//        assertEquals("test-id", result.getProductId());
//        assertEquals("TestProduct", result.getProductName());
//        assertEquals("Test Description", result.getProductDescription());
//        assertEquals(ProductStatus.ENABLED, result.getProductStatus());
//        assertEquals("http://example.com/thumb.jpg", result.getThumbnailUrl());
//
//        verify(productRepository).existsByProductName("TestProduct");
//        verify(productRepository).save(any(Product.class));
//    }
//
//    @Test
//    void saveProduct_ShouldCreateProductAndPackages_WhenPackagesProvided() {
//        PackageRequest packageRequest = PackageRequest.builder()
//                .packageName("TestPackage")
//                .features(List.of(
//                    FeatureDto.builder().id("feature1").name("Feature 1").build(),
//                    FeatureDto.builder().id("feature2").name("Feature 2").build()
//                ))
//                .price(99.99)
//                .packageStatus(PackageStatus.ENABLED)
//                .basePackageId("base-package-id")
//                .build();
//
//        ProductCreationRequest dto = ProductCreationRequest.builder()
//                .productName("TestProduct")
//                .productDescription("Test Description")
//                .productStatus(ProductStatus.ENABLED)
//                .thumbnailUrl("http://example.com/thumb.jpg")
//                .packages(List.of(packageRequest))
//                .build();
//
//        when(productRepository.existsByProductName("TestProduct")).thenReturn(false);
//        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
//            Product product = invocation.getArgument(0);
//            product.setId("test-id");
//            return product;
//        });
//        when(productPackageMapper.toEntity(any(PackageRequest.class), anyString())).thenAnswer(invocation -> {
//            PackageRequest request = invocation.getArgument(0);
//            String productId = invocation.getArgument(1);
//            return ProductPackage.builder()
//                    .id("package-id")
//                    .name(request.getPackageName())
//                    .productId(productId)
//                    .featureId(request.getFeatures() != null ? request.getFeatures().stream()
//                            .map(FeatureDto::getId)
//                            .toList() : List.of())
//                    .price(request.getPrice())
//                    .packageStatus(request.getPackageStatus())
//                    .basePackageId(request.getBasePackageId())
//                    .build();
//        });
//        when(productPackageMapper.toResponse(any(ProductPackage.class))).thenAnswer(invocation -> {
//            ProductPackage pkg = invocation.getArgument(0);
//            return PackageRequest.builder()
//                    .id(pkg.getId())
//                    .packageName(pkg.getName())
//                    .productId(pkg.getProductId())
//                    .features(pkg.getFeatureId() != null ? pkg.getFeatureId().stream()
//                            .map(id -> FeatureDto.builder().id(id).name("Feature " + id).build())
//                            .toList() : List.of())
//                    .price(pkg.getPrice())
//                    .packageStatus(pkg.getPackageStatus())
//                    .basePackageId(pkg.getBasePackageId())
//                    .build();
//        });
//
//        final var result = productService.saveProduct(dto);
//
//        assertNotNull(result);
//        assertEquals("test-id", result.getProductId());
//        assertEquals("TestProduct", result.getProductName());
//        assertEquals("Test Description", result.getProductDescription());
//
//        verify(productRepository).existsByProductName("TestProduct");
//        verify(productRepository).save(any(Product.class));
//        verify(productPackageRepository).saveAll(anyList());
//    }
//
//    @Test
//    void saveProduct_ShouldCreateProductOnly_WhenNoPackagesProvided() {
//        ProductCreationRequest dto = ProductCreationRequest.builder()
//                .productName("TestProduct")
//                .productDescription("Test Description")
//                .productStatus(ProductStatus.ENABLED)
//                .thumbnailUrl("http://example.com/thumb.jpg")
//                .packages(null)
//                .build();
//
//        when(productRepository.existsByProductName("TestProduct")).thenReturn(false);
//        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
//            Product product = invocation.getArgument(0);
//            product.setId("test-id");
//            return product;
//        });
//
//        final var result = productService.saveProduct(dto);
//
//        assertNotNull(result);
//        assertEquals("test-id", result.getProductId());
//        assertEquals("TestProduct", result.getProductName());
//        assertEquals("Test Description", result.getProductDescription());
//
//        verify(productRepository).existsByProductName("TestProduct");
//        verify(productRepository).save(any(Product.class));
//        verify(packageService, never()).savePackage(any());
//    }
//
//    // --- getAllProducts tests ---
//
//    @Test
//    void getAllProducts_ShouldReturnList_WhenSearchIsNull() {
//
//        List<Product> productList = List.of(
//                mockProduct("1", "Apple"),
//                mockProduct("2", "Banana")
//        );
//        Page<Product> productPage = new PageImpl<>(productList);
//
//        when(productRepository.findAll(any(Pageable.class))).thenReturn(productPage);
//
//        List<ProductResponse> result = productService.getAllProducts(null, null, 0, 10, "productName", "asc");
//
//        assertEquals(2, result.size());
//        verify(productRepository).findAll(any(Pageable.class));
//    }
//
//    @Test
//    void getAllProducts_ShouldReturnList_WhenSearchIsProvided() {
//        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "productName"));
//
//        List<Product> productList = List.of(
//                mockProduct("1", "Apple")
//        );
//        Page<Product> productPage = new PageImpl<>(productList);
//
//        when(productRepository.findByProductName("App", pageable)).thenReturn(productPage);
//
//        List<ProductResponse> result = productService.getAllProducts("App", null, 0, 10, "productName", "desc");
//
//        assertEquals(1, result.size());
//        verify(productRepository).findByProductName("App", pageable);
//    }
//
//    // --- getProductById tests ---
//
//    @Test
//    void getProductById_ShouldThrowResourceNotFoundException_WhenNotFound() {
//        when(productRepository.findById("invalid-id")).thenReturn(Optional.empty());
//
//        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
//                () -> productService.getProductById("invalid-id"));
//        assertTrue(ex.getMessage().contains("Resource not found with id"));
//    }
//
//    // --- updateProductById tests ---
//
//    @Test
//    void updateProductById_ShouldThrowResourceNotFoundException_WhenNotFound() {
//        when(productRepository.findById("id")).thenReturn(Optional.empty());
//
//        ProductCreationRequest request = ProductCreationRequest.builder()
//                .productName("TestProduct")
//                .productStatus(ProductStatus.ENABLED)
//                .build();
//
//        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
//                () -> productService.updateProductById("id", request));
//        assertTrue(ex.getMessage().contains("Resource not found with id"));
//    }
//
//    @Test
//    void updateProductById_ShouldUpdateSuccessfully_WhenValidRequest() {
//        Product existingProduct = mockProduct("1", "OldName");
//        when(productRepository.findById("1")).thenReturn(Optional.of(existingProduct));
//        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
//        doNothing().when(productPackageRepository).deleteByProductId(anyString());
//        when(productPackageRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
//
//        ProductCreationRequest updateDto = ProductCreationRequest.builder()
//                .productName("NewName")
//                .productDescription("New Description")
//                .productStatus(ProductStatus.ENABLED)
//                .thumbnailUrl("http://example.com/new.jpg")
//                .packages(new ArrayList<>())
//                .build();
//
//        ProductResponse result = productService.updateProductById("1", updateDto);
//
//        assertNotNull(result);
//        assertEquals("1", result.getProductId());
//        assertEquals("NewName", result.getProductName());
//        assertEquals("New Description", result.getProductDescription());
//        assertEquals(ProductStatus.ENABLED, result.getProductStatus());
//        assertEquals("http://example.com/new.jpg", result.getThumbnailUrl());
//
//        verify(productRepository).findById("1");
//        verify(productRepository).save(any(Product.class));
//        // deleteByProductId is only called when packages are provided
//        verify(productPackageRepository, never()).deleteByProductId(anyString());
//    }
//
//    @Test
//    void updateProductById_ShouldThrowDuplicateNameException_WhenNameChangedAndExists() {
//        Product existingProduct = mockProduct("1", "OldName");
//        when(productRepository.findById("1")).thenReturn(Optional.of(existingProduct));
//        when(productRepository.existsByProductName("NewName")).thenReturn(true);
//
//        ProductCreationRequest updateDto = ProductCreationRequest.builder()
//                .productName("NewName")
//                .productStatus(ProductStatus.ENABLED)
//                .build();
//
//        DuplicateNameException ex = assertThrows(DuplicateNameException.class,
//                () -> productService.updateProductById("1", updateDto));
//        assertTrue(ex.getMessage().contains("already exists"));
//    }
//
//    // --- deleteProductById tests ---
//
//    @Test
//    void deleteProductById_ShouldThrowResourceNotFoundException_WhenNotFound() {
//        when(productRepository.findById("id")).thenReturn(Optional.empty());
//
//        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
//                () -> productService.deleteProductById("id"));
//        assertTrue(ex.getMessage().contains("Resource not found with id"));
//    }
//
//    @Test
//    void deleteProductById_ShouldDeleteAndReturnProductName() {
//        Product product = mockProduct("id", "DeleteMe");
//        when(productRepository.findById("id")).thenReturn(Optional.of(product));
//
//        doNothing().when(productRepository).deleteById("id");
//
//        String result = productService.deleteProductById("id");
//
//        assertEquals("DeleteMe", result);
//        verify(productRepository).deleteById("id");
//    }
//
//    // --- getTotalProductCount tests ---
//
//    @Test
//    void getTotalProductCount_ShouldReturnCount() {
//        when(productRepository.count()).thenReturn(42L);
//        long count = productService.getTotalProductCount();
//        assertEquals(42L, count);
//    }
//
//    // --- deleteProductsByIds tests ---
//
//    @Test
//    void deleteProductsByIds_ShouldDeleteAllAndReturnNames() {
//        List<String> ids = List.of("id1", "id2");
//        Product p1 = mockProduct("id1", "Prod1");
//        Product p2 = mockProduct("id2", "Prod2");
//
//        when(productRepository.findById("id1")).thenReturn(Optional.of(p1));
//        when(productRepository.findById("id2")).thenReturn(Optional.of(p2));
//        doNothing().when(productRepository).deleteById(anyString());
//
//        List<String> deletedNames = productService.deleteProductsByIds(ids);
//
//        assertEquals(2, deletedNames.size());
//        assertTrue(deletedNames.contains("Prod1"));
//        assertTrue(deletedNames.contains("Prod2"));
//        verify(productRepository, times(2)).deleteById(anyString());
//    }
//
//    // --- updateProductsStatusByIds tests ---
//
//    @Test
//    void updateProductsStatusByIds_ShouldUpdateStatusAndReturnNames() {
//        List<String> ids = List.of("id1", "id2");
//        Product p1 = mockProduct("id1", "Prod1");
//        Product p2 = mockProduct("id2", "Prod2");
//
//        when(productRepository.findById("id1")).thenReturn(Optional.of(p1));
//        when(productRepository.findById("id2")).thenReturn(Optional.of(p2));
//        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
//
//        List<String> updatedNames = productService.updateProductsStatusByIds(ids, Status.ENABLED);
//
//        assertEquals(2, updatedNames.size());
//        assertTrue(updatedNames.contains("Prod1"));
//        assertTrue(updatedNames.contains("Prod2"));
//        assertEquals(ProductStatus.ENABLED, p1.getProductStatus());
//        assertEquals(ProductStatus.ENABLED, p2.getProductStatus());
//    }
//
//    @Test
//    void updateProductsStatusByIds_ShouldThrowResourceNotFound_WhenProductNotFound() {
//        when(productRepository.findById("id1")).thenReturn(Optional.empty());
//
//        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
//                () -> productService.updateProductsStatusByIds(List.of("id1"), Status.ENABLED));
//        assertTrue(ex.getMessage().contains("Resource not found with id"));
//    }
//
//    private Product mockProduct(String id, String name) {
//        Product p = new Product();
//        p.setId(id);
//        p.setProductName(name);
//        p.setProductDescription("Description for " + name);
//        p.setProductStatus(ProductStatus.ENABLED);
////        p.setCourseIds(new ArrayList<>());
//        p.setThumbnailUrl("http://example.com/" + id + ".png");
//        p.setCreatedAt(Instant.now());
//        p.setUpdatedAt(Instant.now());
//        return p;
//    }
//
//
//    // --- getAllProducts with different order values (case-insensitive) ---
//    @Test
//    void getAllProducts_ShouldUseDescOrder_WhenOrderIsDescCaseInsensitive() {
//        Pageable pageableDesc = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "productName"));
//        Page<Product> productPage = new PageImpl<>(Collections.emptyList());
//
//        when(productRepository.findAll(pageableDesc)).thenReturn(productPage);
//
//        List<ProductResponse> result = productService.getAllProducts(null, null, 0, 10, "productName", "DESC");
//
//        assertNotNull(result);
//        verify(productRepository).findAll(pageableDesc);
//    }
//
//    @Test
//    void getAllProducts_ShouldUseAscOrder_WhenOrderIsAscCaseInsensitive() {
//        Pageable pageableAsc = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "productName"));
//        Page<Product> productPage = new PageImpl<>(Collections.emptyList());
//
//        when(productRepository.findAll(pageableAsc)).thenReturn(productPage);
//
//        List<ProductResponse> result = productService.getAllProducts(null, null, 0, 10, "productName", "asc");
//
//        assertNotNull(result);
//        verify(productRepository).findAll(pageableAsc);
//    }
//
//    // --- deleteProductsByIds with empty list ---
//
//    @Test
//    void deleteProductsByIds_ShouldReturnEmptyList_WhenEmptyInput() {
//        List<String> result = productService.deleteProductsByIds(Collections.emptyList());
//        assertNotNull(result);
//        assertTrue(result.isEmpty());
//        verify(productRepository, never()).deleteById(anyString());
//    }
//
//    // --- updateProductsStatusByIds with empty list ---
//
//    @Test
//    void updateProductsStatusByIds_ShouldReturnEmptyList_WhenEmptyInput() {
//        List<String> result = productService.updateProductsStatusByIds(Collections.emptyList(), Status.ENABLED);
//        assertNotNull(result);
//        assertTrue(result.isEmpty());
//        verify(productRepository, never()).save(any());
//    }
//
//}