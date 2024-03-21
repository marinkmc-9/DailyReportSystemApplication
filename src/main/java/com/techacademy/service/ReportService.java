package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.repository.ReportRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    @Autowired
    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    // 日報保存
    @Transactional
    public ErrorKinds save(Report report, UserDetail userDetail) {

        // 同一日付チェック
        List<Report> reportList = findByEmployee(userDetail.getEmployee());
        if (reportList != null) {
            for (Report dupReport : reportList) {
                if (dupReport.getReportDate().equals(report.getReportDate())) {
                    return ErrorKinds.DATECHECK_ERROR;
                }
            }
        }

        LocalDateTime now = LocalDateTime.now();
        report.setCreatedAt(now);
        report.setUpdatedAt(now);

        report.setEmployee(userDetail.getEmployee());
        report.setDeleteFlg(false);
        reportRepository.save(report);
        return ErrorKinds.SUCCESS;
    }

    // 日報更新
    @Transactional
    public ErrorKinds update(Report report, Integer id, UserDetail userDetail) {

        Report beforeReport = findById(id);

        // 同一日付チェック
        // 変更先の従業員番号が同一かつ変更された日付が重複していた場合(変更前の日付同一は除外)
        List<Report> reportList = findByEmployee(userDetail.getEmployee());
        if (reportList != null && beforeReport.getEmployee().getCode().equals(userDetail.getEmployee().getCode())
                && !beforeReport.getReportDate().equals(report.getReportDate())) {
            for (Report dupReport : reportList) {
                // 同一IDで作成かつ日付が重複していた場合
                if (dupReport.getReportDate().equals(report.getReportDate())) {
                    return ErrorKinds.DATECHECK_ERROR;
                }
            }
        }

        // 更新処理
        report.setDeleteFlg(false);
        report.setCreatedAt(beforeReport.getCreatedAt());
        LocalDateTime now = LocalDateTime.now();
        report.setUpdatedAt(now);
        report.setEmployee(beforeReport.getEmployee());

        reportRepository.save(report);
        return ErrorKinds.SUCCESS;
    }

    // 日報削除
    @Transactional
    public void delete(Integer id) {

        Report report = findById(id);

        LocalDateTime now = LocalDateTime.now();
        report.setUpdatedAt(now);
        report.setDeleteFlg(true);

        reportRepository.save(report);
    }

    // 日報一覧表示処理
    public List<Report> findAll() {
        return reportRepository.findAll();
    }

    // 日報IDに紐づく検索処理
    public Report findById(Integer id) {
        return reportRepository.findById(id).get();
    }

    // 従業員に紐づく日報検索処理
    public List<Report> findByEmployee(Employee employee) {
        return reportRepository.findByEmployee(employee);
    }

}
