package site.metacoding.miniproject.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockCookie;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import site.metacoding.miniproject.dto.personal.PersonalReqDto.PersonalUpdatReqDto;
import site.metacoding.miniproject.dto.resumes.ResumesReqDto.ResumesInsertReqDto;
import site.metacoding.miniproject.dto.resumes.ResumesReqDto.ResumesUpdateReqDto;
import site.metacoding.miniproject.dto.resumes.ResumesRespDto.ResumesAllRespDto;
import site.metacoding.miniproject.dto.user.UserRespDto.SignPersonalDto;
import site.metacoding.miniproject.dto.user.UserRespDto.SignedDto;
import site.metacoding.miniproject.service.personal.PersonalService;
import site.metacoding.miniproject.utill.JWTToken.CreateJWTToken;
import site.metacoding.miniproject.utill.SHA256;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
public class PersonalApiControllerTest {

    private static final String APPLICATION_JSON = "application/json; charset=utf-8";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private SHA256 sha256;

    @Autowired
    private ResourceLoader loader;

    @Autowired
    private PersonalService personalService;

    @Autowired
    private MockMvc mvc;

    private MockCookie mockCookie;

    @BeforeEach
    public void sessionInit() {

        SignPersonalDto signPersonalDto = new SignPersonalDto();
        signPersonalDto.setPersonalId(1);
        SignedDto<?> signedDto = new SignedDto<>(1, "testusers1", signPersonalDto);

        String JwtToken = CreateJWTToken.createToken(signedDto);
        mockCookie = new MockCookie("Authorization", JwtToken);
    }

    @Test
    @Sql({ "classpath:truncate.sql", "classpath:testsql/insertresumes.sql" })
    public void insertResumes_test() throws Exception { // ????????? ??????
        // given
        ResumesInsertReqDto resumesInsertReqDto = new ResumesInsertReqDto();

        resumesInsertReqDto.setCategoryFrontend(true);
        resumesInsertReqDto.setCategoryBackend(true);
        resumesInsertReqDto.setCategoryDevops(true);
        resumesInsertReqDto.setPortfolioFile("?????????????????????");
        resumesInsertReqDto.setPortfolioSource("http://github.com/asdfqwer");
        resumesInsertReqDto.setOneYearLess(true);
        resumesInsertReqDto.setTwoYearOver(false);
        resumesInsertReqDto.setThreeYearOver(false);
        resumesInsertReqDto.setFiveYearOver(false);
        resumesInsertReqDto.setResumesTitle("???????????????1");
        resumesInsertReqDto.setResumesPicture("????????????");
        resumesInsertReqDto.setResumesIntroduce("????????????1");
        resumesInsertReqDto.setResumesPlace("????????????");

        String filename = "p4.jpg";
        Resource resource = loader.getResource("classpath:/static/images/" + filename);
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpg", resource.getInputStream());

        String body = om.writeValueAsString(resumesInsertReqDto);
        MockMultipartFile multipartBody = new MockMultipartFile("reqDto", "formData", APPLICATION_JSON,
                body.getBytes("utf-8"));

        // when
        ResultActions resultActions = mvc
                .perform(multipart(HttpMethod.POST, "/s/resumes/insert")
                        .file(file)
                        .file(multipartBody)
                        .accept(APPLICATION_JSON)
                        .cookie(mockCookie));

        // then
        MvcResult mvcResult = resultActions.andReturn();
        resultActions.andExpect(jsonPath("$.code").value(1));
        resultActions.andExpect(jsonPath("$.message").value("????????? ?????? ??????"));
        resultActions.andExpect(jsonPath("$.data.resumesTitle").value("???????????????1"));
    }

    @Test
    @Sql({ "classpath:truncate.sql", "classpath:testsql/findallmyresumes.sql" })
    public void findAllMyResumes_test() throws Exception { // ??? ????????? ?????? ??????
        // given
        Integer id = 1;

        // when
        ResultActions resultActions = mvc
                .perform(get("/s/resumes/myList").accept(APPLICATION_JSON)
                        .cookie(mockCookie));

        // then
        MvcResult mvcResult = resultActions.andReturn();
        resultActions.andExpect(jsonPath("$.code").value(1));
        resultActions.andExpect(jsonPath("$.message").value("??? ????????? ?????? ?????? ??????"));
        resultActions.andExpect(jsonPath("$.data.[0].resumesTitle").value("resumes_title_example1"));
    }

    @Test
    @Sql({ "classpath:truncate.sql", "classpath:testsql/oneresumes.sql" })
    public void findByResumesId_test() throws Exception { // ????????? ????????????
        // given
        Integer resumesId = 1;

        // when
        ResultActions resultActions = mvc
                .perform(MockMvcRequestBuilders.get("/resumes/" + resumesId).accept(APPLICATION_JSON)
                        .cookie(mockCookie));

        // then
        MvcResult mvcResult = resultActions.andReturn();
        resultActions.andExpect(jsonPath("$.code").value(1));
        resultActions.andExpect(jsonPath("$.message").value("??? ????????? ?????? ?????? ??????"));
        resultActions.andExpect(jsonPath("$.data.resumesTitle").value("resumes_title_example1"));
    }

    @Test
    @Sql({ "classpath:truncate.sql", "classpath:testsql/oneresumes.sql" })
    public void updateResumes_test() throws Exception { // ????????? ??????
        // given
        Integer resumesId = 1;
        ResumesUpdateReqDto resumesUpdateReqDto = new ResumesUpdateReqDto();
        resumesUpdateReqDto.setCategoryFrontend(true);
        resumesUpdateReqDto.setCategoryBackend(true);
        resumesUpdateReqDto.setCategoryDevops(true);
        resumesUpdateReqDto.setPortfolioFile("???????????????????????????");
        resumesUpdateReqDto.setPortfolioSource("http://github.com/asdfqwer");
        resumesUpdateReqDto.setOneYearLess(true);
        resumesUpdateReqDto.setTwoYearOver(false);
        resumesUpdateReqDto.setThreeYearOver(false);
        resumesUpdateReqDto.setFiveYearOver(false);
        resumesUpdateReqDto.setResumesTitle("????????????????????????????????????");
        resumesUpdateReqDto.setResumesPicture("????????????");
        resumesUpdateReqDto.setResumesIntroduce("????????????1");
        resumesUpdateReqDto.setResumesPlace("????????????");

        String filename = "p4.jpg";
        Resource resource = loader.getResource("classpath:/static/images/" + filename);
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpg", resource.getInputStream());
        String body = om.writeValueAsString(resumesUpdateReqDto);
        MockMultipartFile multipartBody = new MockMultipartFile("resumesUpdateReqDto", "formData", APPLICATION_JSON,
                body.getBytes("utf-8"));

        // when
        ResultActions resultActions = mvc
                .perform(multipart(HttpMethod.PUT, "/s/resumes/update/" + resumesId)
                        .file(file)
                        .file(multipartBody)
                        .accept(APPLICATION_JSON)
                        .cookie(mockCookie));

        // then
        MvcResult mvcResult = resultActions.andReturn();
        resultActions.andExpect(jsonPath("$.code").value(1));
        resultActions.andExpect(jsonPath("$.message").value("????????? ?????? ??????"));
        resultActions.andExpect(jsonPath("$.data.resumesTitle").value("????????????????????????????????????"));
    }

    @Test
    @Sql("classpath:testsql/oneresumes.sql")
    public void deleteResumes_test() throws Exception { // ????????? ??????
        // given
        Integer id = 1;

        // when
        ResultActions resultActions = mvc
                .perform(delete("/s/resumes/delete/" + id)
                        .accept(APPLICATION_JSON)
                        .cookie(mockCookie));

        // then
        MvcResult mvcResult = resultActions.andReturn();
        resultActions.andExpect(jsonPath("$.code").value(1));
        resultActions.andExpect(jsonPath("$.message").value("????????? ?????? ??????"));
    }

    // ???????????????
    @Sql({ "classpath:truncate.sql", "classpath:testsql/selectdetailforpersonal.sql" })
    @Test
    public void findByPersonal_test() throws Exception {
        // given

        // when
        ResultActions resultActions = mvc.perform(get("/s/api/personal/detail")
                .cookie(mockCookie)
                .accept(APPLICATION_JSON));

        // then
        MvcResult mvcResult = resultActions.andReturn();
    }

    // ???????????????
    @Sql({ "classpath:truncate.sql", "classpath:testsql/selectdetailforpersonal.sql" })
    @Test
    public void updatePersonalDetail_test() throws Exception {

        // given
        PersonalUpdatReqDto personalUpdateReqDto = new PersonalUpdatReqDto();
        personalUpdateReqDto.setPersonalName("ssar");
        personalUpdateReqDto.setPersonalPhoneNumber("010-9459-5116");
        personalUpdateReqDto.setPersonalEmail("cndtjq1248@naver.com");
        personalUpdateReqDto.setPersonalAddress("??????,?????????,?????????");
        personalUpdateReqDto.setPersonalEducation("4??????");
        personalUpdateReqDto.setLoginPassword("@@ps990104");
        String body = om.writeValueAsString(personalUpdateReqDto);

        // when
        ResultActions resultActions = mvc.perform(put("/s/api/personal/update").content(body)
                .contentType(APPLICATION_JSON).accept(APPLICATION_JSON).cookie(mockCookie));

        // then
        MvcResult mvcResult = resultActions.andReturn();

    }

    // ?????? ????????? ?????? ??????
    @Sql({ "classpath:truncate.sql", "classpath:testsql/findallmyresumes.sql" })
    @Test
    public void findAllResumes_test() throws Exception {
        ResumesAllRespDto resumesAllRespDto = new ResumesAllRespDto();
        resumesAllRespDto.setId(1);
        resumesAllRespDto.setPage(0);
        resumesAllRespDto.setStartNum(0);
        resumesAllRespDto.setKeyword("s");
        String body = om.writeValueAsString(resumesAllRespDto);
        ResultActions resultActions = mvc.perform(get("/resumes/resumesList/" + resumesAllRespDto.getId())
                .cookie(mockCookie)
                .accept(APPLICATION_JSON));

        // then
        MvcResult mvcResult = resultActions.andReturn();
        resultActions.andExpect(jsonPath("$.code").value(1));
        resultActions.andExpect(jsonPath("$.message").value("?????? ????????? ?????? ?????? ??????"));
        resultActions.andExpect(jsonPath("$.data.[0].resumesTitle").value("resumes_title_example1"));
    }

    @Test
    @Sql({ "classpath:truncate.sql", "classpath:testsql/insertcompanyfortest.sql" })
    public void companyDetailform_test() throws Exception { // ?????? - ?????? ????????????
        // given
        Integer companyId = 1;

        // when
        ResultActions resultActions = mvc
                .perform(MockMvcRequestBuilders.get("/personal/company/" + companyId).accept(APPLICATION_JSON)
                        .cookie(mockCookie));

        // then
        MvcResult mvcResult = resultActions.andReturn();
        resultActions.andExpect(jsonPath("$.code").value(1));
        resultActions.andExpect(jsonPath("$.message").value("??????????????????"));
        resultActions.andExpect(jsonPath("$.data.companyName").value("testCompanyname"));
    }

    @Test
    @Sql({ "classpath:truncate.sql", "classpath:testsql/insertjobpostingBoard.sql" })
    public void jobPostingDetailForm_test() throws Exception { // ?????? - ???????????? ?????? ??????
        // given
        Integer jobPostingBoardId = 1;

        // when
        ResultActions resultActions = mvc
                .perform(MockMvcRequestBuilders.get("/personal/jobPostingBoard/" + jobPostingBoardId)
                        .accept(APPLICATION_JSON)
                        .cookie(mockCookie));

        // then
        MvcResult mvcResult = resultActions.andReturn();
        resultActions.andExpect(jsonPath("$.code").value(1));
        resultActions.andExpect(jsonPath("$.message").value("???????????? ????????????"));
        resultActions.andExpect(jsonPath("$.data.jobPostingBoardTitle").value("title"));
    }
}
