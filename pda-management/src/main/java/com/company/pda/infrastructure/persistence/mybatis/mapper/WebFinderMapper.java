package com.company.pda.infrastructure.persistence.mybatis.mapper;

import com.company.pda.application.pdafinder.dto.WebDeviceRow;
import java.util.List;

public interface WebFinderMapper extends com.company.pda.application.port.out.WebFinderQueries {
  List<WebDeviceRow> devices();
}
