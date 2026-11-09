package org.example.chaenggim.domain.seed.service;

import org.example.chaenggim.domain.doctor.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** 시작 시 chaenggim.seed-on-empty=true이고 의료진(숨김 포함)이 0명이면 seed(). */
@Component
public class SeedRunner implements ApplicationRunner {

    private final SeedService seedService;
    private final DoctorRepository doctorRepository;
    private final boolean seedOnEmpty;

    public SeedRunner(SeedService seedService,
                      DoctorRepository doctorRepository,
                      @Value("${chaenggim.seed-on-empty}") boolean seedOnEmpty) {
        this.seedService = seedService;
        this.doctorRepository = doctorRepository;
        this.seedOnEmpty = seedOnEmpty;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (seedOnEmpty && doctorRepository.count() == 0) {
            seedService.seed();
        }
    }
}
