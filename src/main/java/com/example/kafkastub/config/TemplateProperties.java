package com.example.kafkastub.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * Конфигурация каталога и имен шаблонов.
 *
 * <p>Зачем это нужно:
 * - путь и список файлов можно менять без перекомпиляции (через application.yml);
 * - можно заранее зафиксировать ровно 2 исходных файла шаблонов.
 *
 * <p>Справка по @ConfigurationProperties:
 * https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties
 */
@Validated
@ConfigurationProperties(prefix = "app.templates")
public class TemplateProperties {

    /** Каталог в resources/classpath, например: templates */
    @NotBlank
    private String path;

    /**
     * Явный список файлов-шаблонов (по задаче их будет два).
     * Пример: ["template-flow-1.yaml", "template-flow-2.yaml"]
     */
    @NotEmpty
    private List<String> files = new ArrayList<>();

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }
}
