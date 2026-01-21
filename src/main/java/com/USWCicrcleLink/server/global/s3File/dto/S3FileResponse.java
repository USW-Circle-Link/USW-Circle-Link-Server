package com.USWCicrcleLink.server.global.s3File.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class S3FileResponse {
    String presignedUrl;

    String s3FileName;
}
