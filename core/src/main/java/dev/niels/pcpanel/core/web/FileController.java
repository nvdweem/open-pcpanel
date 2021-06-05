package dev.niels.pcpanel.core.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.Comparator;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FileController {

  @GetMapping("files")
  public List<FileResult> getFiles(@RequestParam(required = false) String path) {
    File[] files;
    if (!StringUtils.hasText(path)) {
      files = File.listRoots();
    } else {
      var currentFile = new File(path);
      files = currentFile.isDirectory() ? currentFile.listFiles() : currentFile.getParentFile().listFiles();
    }
    if (files == null) {
      files = new File[0];
    }

    return StreamEx.of(files).map(FileResult::new).sorted(Comparator.comparing(FileResult::isFolder).reversed().thenComparing(FileResult::getName)).toList();
  }

  @Getter
  public class FileResult {
    private final String name;
    private final String fullPath;
    private final boolean isFolder;

    public FileResult(File file) {
      this.fullPath = file.getAbsolutePath();
      this.name = StringUtils.hasText(file.getName()) ? file.getName() : file.getAbsolutePath();
      this.isFolder = file.isDirectory();
    }
  }
}
