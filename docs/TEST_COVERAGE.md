# TEST_COVERAGE.md — Minted API Test Approach

> **56 test classes | 414 tests | all passing**
> Test infrastructure: JUnit 5, Mockito, H2 in-memory, Spring Boot Test slices

---

## Test Infrastructure

**`src/test/resources/application.properties`**
```properties
spring.test.database.replace=none
spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=value,year,month
spring.jpa.hibernate.ddl-auto=create
spring.flyway.enabled=false
```

**`src/test/java/.../support/TestSecurityConfig.java`** — disables Spring Security for controller slice tests:
```java
@TestConfiguration
public class TestSecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
```

---

## Approach 1 — Controller Tests (`@WebMvcTest`)

**Annotations:**
```java
@WebMvcTest(MyController.class)
@Import(TestSecurityConfig.class)
class MyControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // Mock every bean the controller depends on
    @MockBean MyService myService;
    @MockBean UserRepository userRepository;

    // Always mock these filters (present in every controller test)
    @MockBean JwtAuthFilter jwtAuthFilter;
    @MockBean MdcFilter mdcFilter;
    @MockBean CustomUserDetailsService customUserDetailsService;
    @MockBean JwtUtil jwtUtil;
}
```

**`@BeforeEach` setup — required in every controller test:**
```java
@BeforeEach
void setUp() throws Exception {
    // Pass-through both filters
    doAnswer(inv -> {
        ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1));
        return null;
    }).when(jwtAuthFilter).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class), any(FilterChain.class));
    doAnswer(inv -> {
        ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1));
        return null;
    }).when(mdcFilter).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class), any(FilterChain.class));

    // Every controller has getUserId(authentication) that calls userRepository.findByUsername()
    User user = new User();
    user.setId(1L);
    user.setUsername("alice");
    when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
}
```

**Each test method:**
```java
@Test
@WithMockUser(username = "alice")   // required for any endpoint that calls getUserId()
void getSomething_returns200() throws Exception {
    when(myService.getAll(1L)).thenReturn(List.of(sampleResponse));

    mockMvc.perform(get("/api/v1/something"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.success").value(true))
           .andExpect(jsonPath("$.data[0].name").value("Test"));
}

// POST with body
@Test
@WithMockUser(username = "alice")
void create_returns201() throws Exception {
    when(myService.create(any(), eq(1L))).thenReturn(sampleResponse);

    mockMvc.perform(post("/api/v1/something")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(objectMapper.writeValueAsString(request)))
           .andExpect(status().isCreated());
}

// Multipart upload
@Test
@WithMockUser(username = "alice")
void upload_returns200() throws Exception {
    MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", "data".getBytes());
    mockMvc.perform(multipart("/api/v1/something/upload").file(file).param("accountId", "1"))
           .andExpect(status().isOk());
}

// DELETE returning 204
@Test
@WithMockUser(username = "alice")
void delete_returns204() throws Exception {
    doNothing().when(myService).delete(1L, 1L);
    mockMvc.perform(delete("/api/v1/something/1"))
           .andExpect(status().isNoContent());
}
```

**Notes:**
- Endpoints with no auth (e.g. `GET /template`) do NOT use `@WithMockUser` and do NOT need `userRepository` stub
- `DashboardCardController` uniquely depends on both `DashboardCardService` AND `AnalyticsService` — mock both
- `LlmConfigController` directly injects `LlmModelRepository` — mock it too

---

## Approach 2 — Repository Tests (`@DataJpaTest`)

**Annotations:**
```java
@DataJpaTest
@ActiveProfiles("test")
class MyRepositoryTest {
    @Autowired TestEntityManager em;
    @Autowired MyRepository myRepository;
}
```

**Entity setup pattern:**
```java
@BeforeEach
void setUp() {
    user1 = em.persist(buildUser("alice"));
    user2 = em.persist(buildUser("bob"));
    em.flush();
}

// Helpers build entities manually (no auto-wiring)
private User buildUser(String username) {
    User u = new User();
    u.setUsername(username);
    u.setPassword("hashed");
    u.setIsActive(true);
    u.setForcePasswordChange(false);
    u.setRole("USER");
    return u;
}
```

**Entity dependency chain (persist in this order):**
- `User` → `AccountType` → `Account` → `Transaction` / `BulkImport` / `CreditCardStatement`
- `User` → `Friend` → `SplitTransaction` → `SplitShare`
- `User` → `DashboardCard` / `DashboardConfiguration`

**Test each custom query method:**
```java
@Test
void findByIdAndUserId_ownerFound() {
    MyEntity entity = em.persist(buildEntity(user1));
    em.flush();

    Optional<MyEntity> result = myRepository.findByIdAndUserId(entity.getId(), user1.getId());

    assertThat(result).isPresent();
}

@Test
void findByIdAndUserId_wrongUser_returnsEmpty() {
    MyEntity entity = em.persist(buildEntity(user1));
    em.flush();

    Optional<MyEntity> result = myRepository.findByIdAndUserId(entity.getId(), user2.getId());

    assertThat(result).isEmpty();
}
```

**Critical note — `List.of(Object[])` varargs trap:**
```java
// WRONG — Java treats Object[] as varargs, returns List<Object> not List<Object[]>
List<Object[]> rows = List.of(new Object[]{1L, "name", BigDecimal.TEN});

// CORRECT
List<Object[]> rows = new ArrayList<>();
rows.add(new Object[]{1L, "name", BigDecimal.TEN});
```

---

## Approach 3 — Service Tests (`@ExtendWith(MockitoExtension.class)`)

**Annotations:**
```java
@ExtendWith(MockitoExtension.class)
class MyServiceImplTest {
    @Mock MyRepository myRepository;
    @Mock UserRepository userRepository;
    @Mock NotificationHelper notificationHelper;  // if service sends notifications

    @InjectMocks MyServiceImpl myService;
}
```

**Pattern:**
```java
@BeforeEach
void setUp() {
    user = new User(); user.setId(1L); user.setUsername("alice");
    entity = new MyEntity(); entity.setId(10L); entity.setUser(user);
}

@Test
void getById_found_returnsResponse() {
    when(myRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(entity));
    MyResponse result = myService.getById(10L, 1L);
    assertThat(result.id()).isEqualTo(10L);
}

@Test
void getById_notFound_throwsException() {
    when(myRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> myService.getById(99L, 1L))
        .isInstanceOf(ResourceNotFoundException.class);
}

@Test
void create_savesAndReturns() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(myRepository.save(any())).thenAnswer(inv -> {
        MyEntity e = inv.getArgument(0);
        e.setId(20L);
        return e;
    });
    MyResponse result = myService.create(request, 1L);
    assertThat(result.id()).isEqualTo(20L);
    verify(myRepository).save(any(MyEntity.class));
}

@Test
void delete_softDeletesSetsInactive() {
    when(myRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(entity));
    when(myRepository.save(any())).thenReturn(entity);
    myService.delete(10L, 1L);
    assertThat(entity.getIsActive()).isFalse();
}
```

---

## What NOT to test

- Interfaces, DTOs, Enums, Entities — no business logic, skip
- `@Configuration` classes — covered by Spring context loading
- External API integrations (LLM/Gemini calls) — mock at the service boundary
- Scheduled jobs — covered by the underlying service tests
