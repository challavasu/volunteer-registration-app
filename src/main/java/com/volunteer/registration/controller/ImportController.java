package com.volunteer.registration.controller;

import com.volunteer.registration.service.ExcelImportService;
import com.volunteer.registration.service.ImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;
    private final ExcelImportService excelImportService;

    @GetMapping
    public String showImportPage(Model model) {
        return "import-data";
    }

    @PostMapping("/csv")
    public String importCSVData(@RequestParam(value = "campaigns_file", required = false) MultipartFile campaignsFile,
                                @RequestParam(value = "jobs_file", required = false) MultipartFile jobsFile,
                                @RequestParam(value = "shifts_file", required = false) MultipartFile shiftsFile,
                                @RequestParam(value = "volunteers_file", required = false) MultipartFile volunteersFile,
                                RedirectAttributes redirectAttributes) {
        try {
            if ((campaignsFile == null || campaignsFile.isEmpty()) &&
                (jobsFile == null || jobsFile.isEmpty()) &&
                (shiftsFile == null || shiftsFile.isEmpty()) &&
                (volunteersFile == null || volunteersFile.isEmpty())) {
                redirectAttributes.addFlashAttribute("error", "Please select at least one file to import.");
                return "redirect:/import";
            }

            Map<String, Object> result = importService.importFromCSV(campaignsFile, jobsFile, shiftsFile, volunteersFile);

            if ((Boolean) result.get("success")) {
                redirectAttributes.addFlashAttribute("success", "CSV import completed successfully!");
                redirectAttributes.addFlashAttribute("campaignsImported", result.get("campaignsImported"));
                redirectAttributes.addFlashAttribute("jobsImported", result.get("jobsImported"));
                redirectAttributes.addFlashAttribute("shiftsImported", result.get("shiftsImported"));
                redirectAttributes.addFlashAttribute("volunteersImported", result.get("volunteersImported"));
            } else {
                redirectAttributes.addFlashAttribute("error", result.get("message"));
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error during import: " + e.getMessage());
        }

        return "redirect:/import";
    }

    @PostMapping("/excel")
    public String importExcelData(@RequestParam("excel_file") MultipartFile excelFile,
                                  RedirectAttributes redirectAttributes) {
        try {
            if (excelFile == null || excelFile.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select an Excel file to import.");
                return "redirect:/import";
            }

            Map<String, Object> result = excelImportService.importFromExcel(excelFile);

            if ((Boolean) result.get("success")) {
                redirectAttributes.addFlashAttribute("success", "Excel import completed successfully!");
                redirectAttributes.addFlashAttribute("campaignName", result.get("campaignName"));
                redirectAttributes.addFlashAttribute("jobsImported", result.get("jobsImported"));
                redirectAttributes.addFlashAttribute("shiftsImported", result.get("shiftsImported"));
                redirectAttributes.addFlashAttribute("volunteersImported", result.get("volunteersImported"));
                redirectAttributes.addFlashAttribute("registrationsImported", result.get("registrationsImported"));
            } else {
                redirectAttributes.addFlashAttribute("error", result.get("message"));
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error during import: " + e.getMessage());
        }

        return "redirect:/import";
    }
}
