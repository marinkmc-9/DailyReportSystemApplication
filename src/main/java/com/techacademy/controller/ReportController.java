package com.techacademy.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;

import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.service.ReportService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // 日報一覧画面
    @GetMapping
    public String list(Model model, @AuthenticationPrincipal UserDetail userDetail) {

        List<Report> reportList;
        // 権限が一般の場合は自身の日報のみ表示する
        if (userDetail.getEmployee().getRole() == Employee.Role.GENERAL) {
            reportList = reportService.findByEmployee(userDetail.getEmployee());
        } else {
            reportList = reportService.findAll();
        }
        model.addAttribute("reportList", reportList);
        model.addAttribute("listSize", reportList.size());
        return "reports/list";
    }

    // 日報詳細画面
    @GetMapping(value = "/{id}/")
    public String detail(@PathVariable int id, Model model) {

        model.addAttribute("report", reportService.findById(id));
        return "reports/detail";
    }

    // 日報新規登録画面
    @GetMapping(value = "/add")
    public String create(Report report, Model model, @AuthenticationPrincipal UserDetail userDetail) {

        if (userDetail != null) {
            report = new Report();
            report.setEmployee(userDetail.getEmployee());
        }
        model.addAttribute("report", report);
        return "reports/new";
    }

    // 日報新規登録処理
    @PostMapping(value = "/add")
    public String add(@Validated Report report, BindingResult res, Model model,
            @AuthenticationPrincipal UserDetail userDetail) {

        // 入力チェック
        if (res.hasErrors()) {
            return create(report, model, null);
        }

        ErrorKinds result = reportService.save(report, userDetail);
        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            return create(report, model, null);
        }

        return "redirect:/reports";
    }

    // 日報更新画面
    @GetMapping(value = "/{id}/update")
    public String edit(@PathVariable Integer id, Model model) {

        if (id != null) {
            model.addAttribute("report", reportService.findById(id));
        }

        return "reports/edit";
    }

    // 日報更新処理
    @PostMapping(value = "/{id}/update")
    public String update(@Validated Report report, BindingResult res, @PathVariable Integer id, Model model,
            @AuthenticationPrincipal UserDetail userDetail) {

        // 入力チェック
        if (res.hasErrors()) {
            return edit(null, model);
        }

        ErrorKinds result = reportService.update(report, id, userDetail);
        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            return edit(null, model);
        }

        return "redirect:/reports";
    }

    // 日報削除処理
    @PostMapping(value = "/{id}/delete")
    public String delete(@PathVariable Integer id) {

        reportService.delete(id);
        return "redirect:/reports";
    }

}
