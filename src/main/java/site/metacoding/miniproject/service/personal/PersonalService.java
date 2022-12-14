package site.metacoding.miniproject.service.personal;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import lombok.RequiredArgsConstructor;
import site.metacoding.miniproject.domain.career.Career;
import site.metacoding.miniproject.domain.career.CareerDao;
import site.metacoding.miniproject.domain.category.Category;
import site.metacoding.miniproject.domain.category.CategoryDao;
import site.metacoding.miniproject.domain.company.CompanyDao;
import site.metacoding.miniproject.domain.jobpostingboard.JobPostingBoardDao;
import site.metacoding.miniproject.domain.personal.Personal;
import site.metacoding.miniproject.domain.personal.PersonalDao;
import site.metacoding.miniproject.domain.portfolio.Portfolio;
import site.metacoding.miniproject.domain.portfolio.PortfolioDao;
import site.metacoding.miniproject.domain.resumes.Resumes;
import site.metacoding.miniproject.domain.resumes.ResumesDao;
import site.metacoding.miniproject.domain.users.Users;
import site.metacoding.miniproject.domain.users.UsersDao;
import site.metacoding.miniproject.dto.company.CompanyRespDto.CompanyAddressRespDto;
import site.metacoding.miniproject.dto.company.CompanyRespDto.CompanyDetailRespDto;
import site.metacoding.miniproject.dto.company.CompanyRespDto.CompanyDetailWithPerRespDto;
import site.metacoding.miniproject.dto.jobpostingboard.JobPostingBoardRespDto.JobPostingDetailWithPersonalRespDto;
import site.metacoding.miniproject.dto.personal.PersonalReqDto.PersonalUpdatReqDto;
import site.metacoding.miniproject.dto.personal.PersonalRespDto.PersonalAddressRespDto;
import site.metacoding.miniproject.dto.personal.PersonalRespDto.PersonalDetailRespDto;
import site.metacoding.miniproject.dto.personal.PersonalRespDto.PersonalUpdateFormRespDto;
import site.metacoding.miniproject.dto.personal.PersonalRespDto.PersonalUpdateRespDto;
import site.metacoding.miniproject.dto.resumes.ResumesReqDto.ResumesInsertReqDto;
import site.metacoding.miniproject.dto.resumes.ResumesReqDto.ResumesUpdateReqDto;
import site.metacoding.miniproject.dto.resumes.ResumesRespDto.PagingDto;
import site.metacoding.miniproject.dto.resumes.ResumesRespDto.ResumesAllByIdRespDto;
import site.metacoding.miniproject.dto.resumes.ResumesRespDto.ResumesAllRespDto;
import site.metacoding.miniproject.dto.resumes.ResumesRespDto.ResumesDetailRespDto;
import site.metacoding.miniproject.dto.resumes.ResumesRespDto.ResumesInsertRespDto;
import site.metacoding.miniproject.dto.resumes.ResumesRespDto.ResumesUpdateRespDto;
import site.metacoding.miniproject.exception.ApiException;

@Service
@RequiredArgsConstructor
public class PersonalService {

	private final PersonalDao personalDao;
	private final ResumesDao resumesDao;
	private final CategoryDao categoryDao;
	private final PortfolioDao portfolioDao;
	private final CareerDao careerDao;
	private final UsersDao usersDao;
	private final CompanyDao companyDao;
	private final JobPostingBoardDao jobPostingBoardDao;

	// ????????? ?????? ??????
	@Transactional(rollbackFor = RuntimeException.class)
	public ResumesInsertRespDto insertResumes(ResumesInsertReqDto resumesInsertReqDto) {

		try {
			resumesInsertReqDto.ResumesInsertDtoPictureSet();
		} catch (Exception e) {
			throw new ApiException("???????????? ??? ??????");
		}

		Category categoryPS = resumesInsertReqDto.ResumesInsertReqDtoToCategoryEntity();
		categoryDao.insert(categoryPS);

		Portfolio portfolioPS = resumesInsertReqDto.ResumesInsertReqDtoToPortfolioEntity();
		portfolioDao.insert(portfolioPS);

		Career careerPS = resumesInsertReqDto.ResumesInsertReqDtoToCareerEntity();
		careerDao.insert(careerPS);

		// if ??????
		if (!(categoryPS.getCategoryId() != null && portfolioPS.getPortfolioId() != null
				&& careerPS.getCareerId() != null)) {
			throw new ApiException("????????? ?????? ??????");
		}

		Resumes resumesPS = resumesInsertReqDto.ResumesInsertReqDtoToResumesEntity();
		resumesPS.setPersonalId(resumesInsertReqDto.getPersonalId());
		resumesPS.setCareerId(careerPS.getCareerId());
		resumesPS.setPortfolioId(portfolioPS.getPortfolioId());
		resumesPS.setResumesCategoryId(categoryPS.getCategoryId());
		resumesDao.insert(resumesPS);

		ResumesInsertRespDto resumesInsertRespDto = new ResumesInsertRespDto(resumesPS, categoryPS, careerPS,
				portfolioPS);

		return resumesInsertRespDto;

	}

	// ?????? ????????? ????????? ?????? ??????
	@Transactional(readOnly = true)
	public List<ResumesAllByIdRespDto> findAllMyResumes(ResumesAllByIdRespDto resumesAllByIdRespDto) {
		if (resumesAllByIdRespDto.getPersonalId() == null) {
			throw new ApiException("?????? " + resumesAllByIdRespDto.getPersonalId() + " ????????? ????????????.");
		}
		List<Resumes> resumesList = resumesDao.findAllMyResumes(resumesAllByIdRespDto.getPersonalId());
		List<ResumesAllByIdRespDto> resumesAllByIdRespDtoList = new ArrayList<>();
		for (Resumes resumes : resumesList) {
			resumesAllByIdRespDtoList.add(new ResumesAllByIdRespDto(resumes));
		}
		return resumesAllByIdRespDtoList;
	}

	// ????????? ?????? ??????
	@Transactional(readOnly = true)
	public ResumesDetailRespDto findByResumesId(Integer resumesId) {
		ResumesDetailRespDto resumesDetailRespDto = resumesDao.findByResumesId(resumesId);
		if (resumesDetailRespDto == null) {
			throw new ApiException("?????? ???????????? ???????????? ????????????.");
		}
		return resumesDetailRespDto;
	}

	// ????????? ?????? ??????
	@Transactional(rollbackFor = RuntimeException.class)
	public ResumesUpdateRespDto updateResumes(ResumesUpdateReqDto resumesUpdateReqDto) {

		try {
			resumesUpdateReqDto.ResumesUpdateDtoPictureSet();
		} catch (Exception e) {
			throw new ApiException("???????????? ??? ??????");
		}

		Resumes resumesPS = resumesDao.findById(resumesUpdateReqDto.getResumesId());
		if (resumesPS == null) {
			throw new ApiException("?????? ???????????? ????????????.");
		}
		resumesUpdateReqDto.setCategoryId(resumesPS.getResumesCategoryId());
		resumesUpdateReqDto.setPortfolioId(resumesPS.getPortfolioId());
		resumesUpdateReqDto.setCareerId(resumesPS.getCareerId());
		resumesPS.updateResumes(resumesUpdateReqDto);
		resumesDao.update(resumesPS);

		Category categoryPS = categoryDao.findById(resumesUpdateReqDto.getCategoryId());
		categoryPS.updateCategory(resumesUpdateReqDto);
		categoryDao.update(categoryPS);

		Portfolio portfolioPS = portfolioDao.findById(resumesUpdateReqDto.getPortfolioId());
		portfolioPS.updatePortfolio(resumesUpdateReqDto);
		portfolioDao.update(portfolioPS);

		Career careerPS = careerDao.findById(resumesUpdateReqDto.getCareerId());
		careerPS.updateCareer(resumesUpdateReqDto);
		careerDao.update(careerPS);

		ResumesUpdateRespDto resumesUpdateRespDto = new ResumesUpdateRespDto(resumesPS, categoryPS, careerPS,
				portfolioPS);

		return resumesUpdateRespDto;
	}

	// ????????? ??????
	@Transactional
	public void deleteResumes(@PathVariable Integer resumesId) {
		Resumes resumes = resumesDao.findById(resumesId);
		if (resumes == null) {
			throw new RuntimeException("?????? " + resumesId + "??? ????????? ??? ??? ????????????.");
		}
		resumesDao.deleteById(resumesId);
	}

	// ?????? ????????? ?????? ?????? (?????????+??????+????????????id???)
	@Transactional(readOnly = true)
	public List<ResumesAllRespDto> findAllResumes(ResumesAllRespDto resumesAllRespDto) {
		if (resumesAllRespDto.getKeyword() == null || resumesAllRespDto.getKeyword().isEmpty()) {
			List<Resumes> resumesList = resumesDao.findCategory(resumesAllRespDto.getStartNum(),
					resumesAllRespDto.getId());
			List<ResumesAllRespDto> resumesAllRespDtoList = new ArrayList<>();
			for (Resumes resumes : resumesList) {
				resumesAllRespDtoList.add(new ResumesAllRespDto(resumes));
			}
			PagingDto paging = resumesDao.resumesPaging(resumesAllRespDto.getPage(), resumesAllRespDto.getKeyword());
			paging.makeBlockInfo(resumesAllRespDto.getKeyword());
			return resumesAllRespDtoList;
		} else {
			List<Resumes> resumesList = resumesDao.findCategorySearch(resumesAllRespDto.getStartNum(),
					resumesAllRespDto.getKeyword(), resumesAllRespDto.getId());
			List<ResumesAllRespDto> resumesAllRespDtoList = new ArrayList<>();
			for (Resumes resumes : resumesList) {
				resumesAllRespDtoList.add(new ResumesAllRespDto(resumes));
			}
			PagingDto paging = resumesDao.resumesPaging(resumesAllRespDto.getPage(), resumesAllRespDto.getKeyword());
			paging.makeBlockInfo(resumesAllRespDto.getKeyword());
			return resumesAllRespDtoList;
		}
	}

	// ?????? ?????? ??????
	@Transactional(readOnly = true)
	public PersonalDetailRespDto findByPersonal(Integer personalId) {
		Personal personalPS = personalDao.personaldetailById(personalId);
		if (personalPS == null) {
			throw new ApiException("?????? ????????? ?????? ??? ????????????.");
		}
		PersonalDetailRespDto personalDetailRespDto = new PersonalDetailRespDto(personalPS);
		String address = personalPS.getPersonalAddress();
		String[] arry = address.split(",");
		for (int i = 0; i < arry.length; i++) {
			personalDetailRespDto.setZoneCode(arry[0]);
			personalDetailRespDto.setRoadJibunAddr(arry[1]);
			personalDetailRespDto.setDetailAddress(arry[2]);
		}

		return personalDetailRespDto;
	}

	// ??? ?????? ???????????? ????????? ????????????
	@Transactional(readOnly = true)
	public PersonalUpdateFormRespDto personalUpdateById(Integer personalId) {
		PersonalAddressRespDto personalAddressRespDto = personalDao.personalAddressById(personalId);

		PersonalUpdateFormRespDto personalUpdatePS = personalDao.personalUpdateById(personalId);
		if (personalUpdatePS == null) {
			throw new ApiException("?????? ????????? ????????????.");
		}
		PersonalUpdateFormRespDto personalUpdateFormRespDto = new PersonalUpdateFormRespDto(personalUpdatePS,
				personalAddressRespDto);

		return personalUpdateFormRespDto;
	}

	@Transactional(readOnly = true)
	public PersonalAddressRespDto personalAddressRespDto(Integer personalId) {
		return personalDao.personalAddressById(personalId);
	}

	// ??? ?????? ??????
	@Transactional(rollbackFor = Exception.class)
	public PersonalUpdateRespDto updatePersonal(Integer userId, Integer perosnalId,
			PersonalUpdatReqDto personalUpdatReqDto) {

		// user???????????? ??????
		Users personalUserPS = usersDao.findById(userId);

		// personal ???????????? ??????
		Personal personalPS = personalDao.findById(perosnalId);

		if (personalUserPS == null || personalPS == null) {
			throw new ApiException("?????? ????????? ????????????.");
		}
		personalUserPS.update(personalUpdatReqDto.personalPassWordUpdateReqDto());
		usersDao.update(personalUserPS);
		personalPS.updatePersonal(personalUpdatReqDto);
		personalDao.update(personalPS);
		PersonalUpdateRespDto personalUpdateRespDto = new PersonalUpdateRespDto(personalPS, personalUserPS);

		return personalUpdateRespDto;
	}

	// ???????????? ?????? ??????(??????)
	@Transactional(readOnly = true)
	public JobPostingDetailWithPersonalRespDto jobPostingBoardDetail(Integer jobPostingBoardId) {
		JobPostingDetailWithPersonalRespDto jobPostingBoardDetailRespDto = jobPostingBoardDao
				.findByJobPostingBoardToPer(jobPostingBoardId);
		if (jobPostingBoardDetailRespDto == null) {
			throw new ApiException("?????? ??????????????? ???????????? ????????????.");
		}
		Timestamp ts = jobPostingBoardDetailRespDto.getJobPostingBoardDeadline();
		Date date = new Date();
		date.setTime(ts.getTime());
		String formattedDate = new SimpleDateFormat("yyyy???MM???dd???").format(date);
		jobPostingBoardDetailRespDto.setFormatDeadLine(formattedDate);
		return jobPostingBoardDetailRespDto;
	}

	@Transactional(readOnly = true)
	public CompanyDetailWithPerRespDto findByCompany(Integer companyId) {
		CompanyDetailWithPerRespDto companyPS = companyDao.findByCompanyToPer(companyId);
		if (companyPS == null) {
			throw new ApiException("?????? ????????? ?????? ??? ????????????.");
		}
		CompanyDetailWithPerRespDto companyDetailRespDto = new CompanyDetailWithPerRespDto(companyPS);
		String address = companyDetailRespDto.getCompanyAddress();
		String[] arry = address.split(",");
		for (int i = 0; i < arry.length; i++) {
			companyDetailRespDto.setZoneCode(arry[0]);
			companyDetailRespDto.setRoadJibunAddr(arry[1]);
			companyDetailRespDto.setDetailAddress(arry[2]);
		}

		return companyDetailRespDto;
	}
}
