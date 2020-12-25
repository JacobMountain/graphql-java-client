package co.uk.jacobmountain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.processing.Filer;

@Slf4j
@RequiredArgsConstructor
public class FileWriter {

    private final Filer filer;

    public void write(PojoBuilder builder) {
        try {
            builder.build().writeTo(filer);
        } catch (Exception e) {
            log.error("Failed to create class", e);
        }
    }

}
