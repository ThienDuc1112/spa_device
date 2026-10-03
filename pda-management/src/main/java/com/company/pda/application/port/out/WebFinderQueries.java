package com.company.pda.application.port.out;

import com.company.pda.application.pdafinder.dto.WebDeviceRow;
import java.util.List;

public interface WebFinderQueries {
  List<WebDeviceRow> devices();
}
