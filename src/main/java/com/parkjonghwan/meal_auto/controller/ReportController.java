package com.parkjonghwan.meal_auto.controller;

import com.parkjonghwan.meal_auto.domain.Member;
import com.parkjonghwan.meal_auto.dto.DailyReportResponse;
import com.parkjonghwan.meal_auto.service.DailyReportService;
import com.parkjonghwan.meal_auto.service.MemberService;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReportController {
  private final DailyReportService reportService;
  private final MemberService memberService;

  public ReportController(DailyReportService reportService, MemberService memberService) {
    this.reportService = reportService;
    this.memberService = memberService;
  }

  @GetMapping("/reports")
  public List<DailyReportResponse> reports(Principal principal,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    Member member = memberService.get(principal.getName());
    return reportService.getReports(member, from, to).stream()
        .map(DailyReportResponse::from)
        .toList();
  }
}
