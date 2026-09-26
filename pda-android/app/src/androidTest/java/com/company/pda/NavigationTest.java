package com.company.pda;

import static org.junit.Assert.*;

import androidx.lifecycle.ViewModelProvider;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.company.pda.data.remote.dto.auth.AuthDto;
import com.company.pda.domain.model.Product;
import com.company.pda.presentation.auth.LoginActivity;
import com.company.pda.presentation.home.*;
import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NavigationTest {
  private PdaApplication app;

  @Before
  public void clearSession() {
    app = ApplicationProvider.getApplicationContext();
    app.modules().tokens.logout();
    if (android.os.Build.VERSION.SDK_INT >= 33) {
      androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
          .getUiAutomation()
          .grantRuntimePermission(app.getPackageName(), android.Manifest.permission.POST_NOTIFICATIONS);
    }
  }

  @After
  public void logout() {
    app.modules().tokens.logout();
  }

  @Test
  public void loginScreenInflatesAfterPackageSplit() {
    try (var activity = ActivityScenario.launch(LoginActivity.class)) {
      activity.onActivity(screen -> {
        assertTrue(screen.findViewById(R.id.login_username).isShown());
        assertTrue(screen.findViewById(R.id.login_password).isShown());
        assertTrue(screen.findViewById(R.id.login_submit).isShown());
      });
    }
  }

  @Test
  public void imageNavigationAndRotationPreserveScanOrder() {
    var tokens = new AuthDto.Tokens();
    tokens.accessToken = "local-ui-test";
    tokens.refreshToken = "local-ui-test";
    app.modules().tokens.tokens(tokens);
    try (var scenario = ActivityScenario.launch(HomeActivity.class)) {
      scenario.onActivity(
          activity -> {
            var vm = new ViewModelProvider(activity).get(HomeViewModel.class);
            var product = new Product();
            product.barcode = "00123";
            product.productCode = "SKU-1";
            product.productName = "Fixture";
            vm.select(product);
            vm.navigate("image");
          });
      scenario.recreate();
      scenario.onActivity(
          activity -> {
            var vm = new ViewModelProvider(activity).get(HomeViewModel.class);
            assertEquals("00123", vm.selected.barcode);
            assertEquals(1, vm.scanned.size());
            assertEquals("image", vm.screen.getValue());
            vm.navigate("product");
          });
      scenario.onActivity(
          activity -> {
            var vm = new ViewModelProvider(activity).get(HomeViewModel.class);
            assertEquals("SKU-1", vm.selected.productCode);
            assertEquals(1, vm.scanned.size());
          });
    }
  }
}
