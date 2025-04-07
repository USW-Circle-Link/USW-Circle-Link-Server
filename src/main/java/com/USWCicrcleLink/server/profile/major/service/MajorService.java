package com.USWCicrcleLink.server.profile.major.service;

import com.USWCicrcleLink.server.profile.major.domain.College;
import com.USWCicrcleLink.server.profile.major.domain.Major;
import com.USWCicrcleLink.server.profile.major.dto.MajorListResponse;
import com.USWCicrcleLink.server.profile.major.dto.MajorResponse;
import com.USWCicrcleLink.server.profile.major.repository.CollegeRepository;
import com.USWCicrcleLink.server.profile.major.repository.MajorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MajorService {
    private final CollegeRepository collegeRepository;
    private final MajorRepository majorRepository;

    public List<MajorListResponse> getAllMajorList(){
        List<College> colleges = collegeRepository.findAll();
        List<MajorListResponse> majorListResponses = new ArrayList<>();

        for(College college : colleges) {
            List<Major> majors = majorRepository.findByCollege(college);
            List<MajorResponse> majorResponses = majors.stream()
                    .map(major -> new MajorResponse(major.getMajorId(),major.getMajorName()))
                    .collect(Collectors.toList());

            majorListResponses.add(new MajorListResponse(college.getCollegeId(),college.getCollegeName(),majorResponses));
        }

        return majorListResponses;
    }
}
