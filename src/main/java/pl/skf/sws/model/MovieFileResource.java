package pl.skf.sws.model;

import org.springframework.core.io.Resource;

public record MovieFileResource(
        Resource resource, String filename) {

}
