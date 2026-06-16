package com.green.member.application.member;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.green.common.auth.MemberContext;
import com.green.common.enumcode.EnumMemberRole;
import com.green.common.model.MemberDto;
import com.green.common.security.JwtTokenManager;
import com.green.member.application.member.model.MemberProfileRes;
import com.green.member.application.member.model.MemberUpdateReq;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.http.converter.autoconfigure.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = MemberController.class,
        excludeAutoConfiguration = com.green.common.enumcode.EnumAutoConfiguration.class
)
@AutoConfigureRestDocs
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(HttpMessageConvertersAutoConfiguration.class)
class MemberControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            // 💡 이 설정을 추가해야 LocalDate가 [2026,3,2]가 아닌 "2026-03-02" 문자열로 변환됩니다.
            mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return mapper;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private JwtTokenManager jwtTokenManager;

    @Test
    @DisplayName("로그인 멤버 프로파일 조회 API 테스트 및 문서화")
    void findLoginUserProfile() throws Exception {
        // given
        MemberDto mockMemberDto = mock(MemberDto.class);
        when(mockMemberDto.memberCode()).thenReturn(1L);
        when(mockMemberDto.role()).thenReturn(EnumMemberRole.STUDENT);

        MemberProfileRes mockResponse = MemberProfileRes.builder()
                .memberCode(1L)
                .role("STUDENT")
                .name("홍길동")
                .birth(LocalDate.of(2000, 1, 1))
                .pic("profile.png")
                .email("hong@test.com")
                .tel("010-1234-5678")
                .emergencyTel("010-9876-5432")
                .postcode("12345")
                .address("대구시 중구")
                .detailAddress("중앙대로 123")
                .entryDate(LocalDate.of(2026, 3, 2))
                .exitDate(null)
                .build();

        // 💡 해결: eq() 대신 any()를 사용하여 매핑 미스매치 방지
        when(memberService.getMyProfile(any(), any())).thenReturn(mockResponse);

        try (MockedStatic<MemberContext> memberContextMockedStatic = mockStatic(MemberContext.class)) {
            memberContextMockedStatic.when(MemberContext::get).thenReturn(mockMemberDto);

            // when
            ResultActions result = mockMvc.perform(get("/api/member/my")
                    .accept(MediaType.APPLICATION_JSON));

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("프로파일 조회"))
                    .andDo(print())
                    .andDo(document("find-login-user-profile",
                            resource(ResourceSnippetParameters.builder()
                                    .tag("Member")
                                    .summary("로그인 멤버 프로파일 조회")
                                    .description("현재 로그인된 회원의 상세 프로필 정보를 조회합니다.")
                                    .responseFields(
                                            fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메시지"),
                                            fieldWithPath("data.memberCode").type(JsonFieldType.NUMBER).description("회원 고유 코드"),
                                            fieldWithPath("data.role").type(JsonFieldType.STRING).description("회원 권한/역할"),
                                            fieldWithPath("data.name").type(JsonFieldType.STRING).description("회원 이름"),
                                            fieldWithPath("data.birth").type(JsonFieldType.STRING).description("생년월일 (YYYY-MM-DD)"),
                                            fieldWithPath("data.pic").type(JsonFieldType.STRING).description("프로필 이미지 파일명").optional(),
                                            fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일 주소"),
                                            fieldWithPath("data.tel").type(JsonFieldType.STRING).description("전화번호"),
                                            fieldWithPath("data.emergencyTel").type(JsonFieldType.STRING).description("비상 연락처"),
                                            fieldWithPath("data.postcode").type(JsonFieldType.STRING).description("우편번호"),
                                            fieldWithPath("data.address").type(JsonFieldType.STRING).description("기본 주소"),
                                            fieldWithPath("data.detailAddress").type(JsonFieldType.STRING).description("상세 주소"),
                                            fieldWithPath("data.entryDate").type(JsonFieldType.STRING).description("등록일/입사일 (YYYY-MM-DD)"),
                                            fieldWithPath("data.exitDate").type(JsonFieldType.STRING).description("퇴사일/종료일 (YYYY-MM-DD)").optional()
                                    )
                                    .build()
                            )
                    ));
        }
    }

    @Test
    @DisplayName("로그인 멤버 내 정보 수정 API 테스트 및 문서화")
    void updateMyProfile() throws Exception {
        // given
        MemberDto mockMemberDto = mock(MemberDto.class);
        when(mockMemberDto.memberCode()).thenReturn(1L);
        when(mockMemberDto.role()).thenReturn(EnumMemberRole.PROFESSOR);

        MemberUpdateReq request = new MemberUpdateReq();
        request.setTel("010-1111-2222");
        request.setEmergencyTel("010-3333-4444");
        request.setPostcode("54321");
        request.setAddress("대구시 북구");
        request.setDetailAddress("대학로 80");
        request.setEmail("professor@test.com");

        // 💡 해결: eq() 대신 any()를 사용하여 매핑 미스매치 방지
        doNothing().when(memberService).updateMyProfile(any(), any(), any(), any());

        MockMultipartFile pic = new MockMultipartFile(
                "pic",
                "profile.png",
                MediaType.IMAGE_PNG_VALUE,
                "image-content".getBytes()
        );

        MockMultipartFile req = new MockMultipartFile(
                "req",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
        );

        try (MockedStatic<MemberContext> memberContextMockedStatic = mockStatic(MemberContext.class)) {
            memberContextMockedStatic.when(MemberContext::get).thenReturn(mockMemberDto);

            // when
            ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.multipart("/api/member/my")
                    .file(req)
                    .file(pic)
                    .with(builder -> { builder.setMethod("PATCH"); return builder; }));

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("내 정보 수정을 완료했습니다"))
                    .andDo(print())
                    .andDo(MockMvcRestDocumentationWrapper.document("update-my-profile",
                            resource(ResourceSnippetParameters.builder()
                                    .tag("Member")
                                    .summary("로그인 멤버 내 정보 수정")
                                    .description("현재 로그인된 회원의 프로필 정보를 수정합니다. (MultipartForm 데이터)")
                                    .responseFields(
                                            fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메시지"),
                                            fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터 (null)").optional()
                                    )
                                    .build()
                            ),
                            requestParts(
                                    partWithName("req").description("수정할 프로필 데이터 JSON"),
                                    partWithName("pic").description("변경할 프로필 이미지 파일").optional()
                            ),
                            requestPartFields("req",
                                    fieldWithPath("tel").type(JsonFieldType.STRING).description("전화번호").optional(),
                                    fieldWithPath("emergencyTel").type(JsonFieldType.STRING).description("비상 연락처").optional(),
                                    fieldWithPath("postcode").type(JsonFieldType.STRING).description("우편번호").optional(),
                                    fieldWithPath("address").type(JsonFieldType.STRING).description("기본 주소").optional(),
                                    fieldWithPath("detailAddress").type(JsonFieldType.STRING).description("상세 주소").optional(),
                                    fieldWithPath("email").type(JsonFieldType.STRING).description("이메일 주소").optional(),
                                    fieldWithPath("labBuilding").type(JsonFieldType.STRING).description("연구실 건물 고유 코드 (교수 전용)").optional(),
                                    fieldWithPath("labRoom").type(JsonFieldType.STRING).description("연구실 호수 (교수 전용)").optional(),
                                    fieldWithPath("labTel").type(JsonFieldType.STRING).description("연구실 전화번호 (교수 전용)").optional()
                            )
                    ));
        }
    }
}