# So sánh Polling và FCM cho chức năng tìm PDA

Tài liệu này giúp khách hàng lựa chọn cách gửi lệnh tìm và dừng chuông trên PDA, dựa trên điều kiện thiết bị, mạng và cách vận hành cửa hàng. Phần mô tả hệ thống hiện tại được đối chiếu với mã nguồn ngày 05/10/2026; các đề xuất mở rộng được ghi riêng để tránh hiểu nhầm là chức năng đã có.

**FCM phù hợp để ưu tiên thử nghiệm khi PDA hỗ trợ dịch vụ Google, có kết nối Internet phù hợp và cần nhận lệnh nhanh, hạn chế việc gọi máy chủ liên tục. Polling phù hợp khi thiết bị không đáp ứng điều kiện FCM hoặc doanh nghiệp muốn nhận lệnh trực tiếp từ máy chủ nội bộ, đồng thời có thể quản lý việc chạy nền và mức tiêu thụ pin.**

Việc thường xuyên điều chuyển PDA giữa các cửa hàng không tự làm một phương án tốt hơn phương án còn lại. Trong cả hai trường hợp, hệ thống vẫn cần biết máy đang thuộc cửa hàng nào, ai được phép tìm máy và các lệnh trước khi bàn giao phải được xử lý ra sao.

## 1. Hai phương án hoạt động như thế nào?

Hãy hình dung quản lý bấm **Tìm PDA** trên website.

Với **polling**, PDA định kỳ hỏi máy chủ: “Có lệnh nào dành cho tôi không?”. Nếu có, máy lấy lệnh và bật chuông. Nếu quản lý vừa bấm Tìm ngay sau một lần kiểm tra, phải chờ lần kiểm tra tiếp theo.

Với **FCM** — Firebase Cloud Messaging của Google — máy chủ chuyển lệnh qua dịch vụ Google để gửi tới ứng dụng trên PDA. PDA không phải liên tục gọi API hỏi xem có lệnh mới hay chưa. Khi nhận được lệnh hợp lệ, ứng dụng bật chuông.

| Polling | FCM |
| --- | --- |
| Quản lý bấm Tìm → máy chủ lưu lệnh → PDA hỏi và nhận lệnh → app bật chuông. | Quản lý bấm Tìm → máy chủ lưu lệnh → Google chuyển lệnh tới PDA → app bật chuông. |

Ở cả hai phương án, **app trên PDA mới là thành phần phát âm thanh**. Cách chuyển lệnh không giải quyết thay các vấn đề như hết pin, loa hỏng hoặc cài đặt Không làm phiền. PDA cũng phải gửi phản hồi về máy chủ để website biết kết quả.

Phạm vi polling trong tài liệu là **PDA định kỳ lấy lệnh bằng HTTP**, như bản ứng dụng hiện có. Việc website tự làm mới danh sách thiết bị là một luồng khác và vẫn có thể tồn tại khi PDA dùng FCM.

## 2. So sánh từ góc nhìn khách hàng

| Góc nhìn | Polling | FCM | Ý nghĩa khi lựa chọn |
| --- | --- | --- | --- |
| Thời gian từ lúc bấm Tìm đến lúc máy kêu | Có thời gian chờ lượt hỏi tiếp theo. Nhịp hỏi càng ngắn thì càng nhiều request. | Không chờ lượt hỏi định kỳ; thời gian thực tế còn phụ thuộc máy chủ, Google, mạng và PDA. | FCM có lợi thế về cơ chế phản hồi, nhưng cần đo trên thiết bị thật. |
| Sử dụng cả ca làm việc | App phải duy trì việc kiểm tra lệnh, kể cả khi không ai tìm máy. | App không cần duy trì vòng hỏi lệnh HTTP. | FCM thường có lợi thế về mức hoạt động nền cho chức năng này; chưa thể quy ra phần trăm pin nếu chưa đo. |
| Thiết bị đa dạng | Đường nhận lệnh HTTP không cần FCM; vẫn phải tương thích Android và chính sách chạy nền của từng hãng. | Cần môi trường thiết bị hỗ trợ Firebase Messaging và dịch vụ Google phù hợp. | Cần kiểm tra đúng model và bản firmware đang mua hoặc đang sử dụng. |
| Mạng chỉ cho phép truy cập máy chủ doanh nghiệp | Có thể phù hợp nếu PDA truy cập được API qua LAN hoặc mạng riêng. | Cần thêm đường kết nối tới dịch vụ Google. | Đây có thể là điều kiện quyết định ngay từ đầu. |
| Nhiều cửa hàng, nhiều PDA | Lưu lượng hỏi lệnh tăng theo số máy và tần suất hỏi, ngay cả lúc không phát sinh yêu cầu tìm. | Giảm được lượng hỏi lệnh định kỳ, nhưng máy chủ vẫn phải xử lý yêu cầu và phản hồi. | FCM thường thuận lợi hơn về tải nhận lệnh khi quy mô tăng và số lần tìm ít. |
| Quản lý vận hành | Theo dõi API, nhịp gọi, service nền và chính sách pin. | Theo dõi thêm cấu hình Firebase, token thiết bị và tình trạng giao lệnh. | Cả hai đều cần công cụ chẩn đoán; loại lỗi thường gặp khác nhau. |
| Đổi cửa hàng, đổi người sử dụng | Cần cập nhật phân công thiết bị và quyền trên máy chủ. | Cũng cần cập nhật phân công và quyền; token FCM không đại diện cho cửa hàng. | Nên đánh giá quy trình bàn giao riêng với cách gửi lệnh. |
| Độ chắc chắn của kết quả | Nhận được HTTP không đồng nghĩa loa đã kêu. | Google chấp nhận gửi không đồng nghĩa PDA đã nhận hoặc loa đã kêu. | Cần phản hồi từ PDA và kiểm thử âm thanh thực tế ở cả hai phương án. |

Yêu cầu nền tảng FCM được mô tả trong [hướng dẫn Android của Firebase](https://firebase.google.com/docs/cloud-messaging/android/get-started). Không nên suy ra mọi PDA chạy Android đều dùng được FCM.

## 3. Tốc độ, độ tin cậy và những tình huống dễ hiểu nhầm

### Bấm Tìm thì bao lâu máy sẽ kêu?

Với một hệ thống polling giả định chạy đều mỗi 5 giây, thời gian chờ đến lượt hỏi tiếp theo nằm trong khoảng gần 0–5 giây, trung bình khoảng 2,5 giây nếu thời điểm bấm phân bố đều. Đây chỉ là **thời gian chờ lượt hỏi**, chưa cộng mạng, máy chủ và thời gian bật chuông.

Bản ứng dụng hiện tại chờ 5 giây **sau khi một lượt gọi thành công kết thúc**, nên chu kỳ thực tế dài hơn 5 giây. Khi gặp lỗi, khoảng chờ tăng dần, tối đa 60 giây. Vì vậy không thể cam kết “polling 5 giây nghĩa là chắc chắn kêu trong 5 giây”.

FCM không có khoảng chờ polling này, nhưng vẫn có các bước xử lý và hàng đợi. Message ưu tiên cao hỗ trợ giao lệnh kịp thời; đó không phải cam kết nhận ngay trong mọi trạng thái thiết bị. Xem [cơ chế ưu tiên của FCM](https://firebase.google.com/docs/cloud-messaging/android-message-priority).

### Máy khóa màn hình hoặc để lâu không dùng

Polling hiện tại dùng service nền có thông báo thường trực và cơ chế giữ CPU hoạt động. Tuy nhiên, Android có thể hạn chế mạng khi thiết bị vào chế độ tiết kiệm pin; chỉ có service hoặc wake lock chưa đủ để bảo đảm nhận lệnh liên tục. Cần kiểm tra chính sách thiết bị của hãng và hệ thống quản lý thiết bị tập trung, nếu có.

FCM được thiết kế để hỗ trợ giao message khi thiết bị nghỉ, dùng kết nối được chia sẻ qua hệ thống. Đây là lợi thế về chạy nền, nhưng vẫn cần thử nghiệm trên từng dòng PDA. Tham khảo [Android: Doze và App Standby](https://developer.android.com/training/monitoring-device-state/doze-standby).

Đóng màn hình ứng dụng, hệ điều hành dọn tiến trình và người dùng bấm **Buộc dừng** là các tình huống khác nhau. Không nên lấy kết quả thử khi app đang mở để kết luận máy sẽ nhận lệnh tốt trong cả ca. Với bản polling hiện có, sau khi khởi động lại máy cần mở Home để khởi động luồng nhận lệnh; chưa có cơ chế tự khởi động polling sau reboot.

### Mất mạng rồi có mạng lại

Nếu PDA không còn đường kết nối phù hợp, cả hai đều không nhận được lệnh mới. Polling có thể tiếp tục hoạt động khi Internet ngoài cửa hàng bị đứt **nếu API vẫn truy cập được trong mạng nội bộ**. Nếu máy chủ đặt trên cloud và đường tới máy chủ cũng mất, lợi thế này không còn.

Khi kết nối trở lại, chỉ nên thực hiện lệnh còn hiệu lực. Ví dụ, quản lý tìm máy lúc 10:00 với hạn 60 giây, đến 10:05 máy mới có mạng thì không nên tự kêu cho yêu cầu cũ. Hệ thống hiện tại kiểm tra hạn lệnh trên PDA; phía FCM còn đặt thời gian lưu message tương ứng. Xem [thời hạn message FCM](https://firebase.google.com/docs/cloud-messaging/customize-messages/setting-message-lifespan).

### Bấm Dừng nhưng máy chưa dừng

Lệnh Dừng cũng phải được giao tới thiết bị. Polling chờ lượt hỏi tiếp theo; FCM phụ thuộc việc giao message. Nếu PDA mất mạng trong khi đang reo, người dùng cần dừng tại máy hoặc chờ thời hạn tự dừng. Website ghi nhận yêu cầu dừng không có nghĩa loa đã dừng ngay.

Tương tự, “chưa nhận được phản hồi” cũng chưa đủ để kết luận máy không kêu: PDA có thể đã bật chuông nhưng phản hồi HTTP bị chậm. Nên phân biệt trên giao diện các mốc **đã tạo yêu cầu**, **đã gửi**, **PDA báo đang phát chuông** và **PDA báo đã dừng**. Ngay cả phản hồi phần mềm cũng cần được kiểm chứng bằng việc nghe chuông trong đợt nghiệm thu.

## 4. Khi chuyển PDA từ cửa hàng A sang cửa hàng B

Giả sử PDA-023 đang thuộc cửa hàng A và được bàn giao cho cửa hàng B. Sau bàn giao, B phải tìm được máy; quyền thao tác của A phải tuân theo chính sách mới, còn lịch sử sử dụng vẫn cần tra cứu được.

**Mang máy sang địa điểm mới, đổi Wi-Fi hoặc đăng nhập bằng nhân viên B không tự chứng minh rằng việc điều chuyển trên hệ thống đã hoàn tất.** Cần phân biệt vị trí thực tế, tài khoản đang dùng và cửa hàng được gán cho thiết bị trong dữ liệu quản lý.

### Khác biệt giữa hai cách nhận lệnh

| Vấn đề khi bàn giao | Polling | FCM |
| --- | --- | --- |
| Máy nhận lệnh theo địa chỉ nào? | PDA gọi API bằng định danh và thông tin xác thực thiết bị. | Máy chủ gửi tới token của bản cài app trên PDA. |
| Đổi cửa hàng có bắt buộc đổi địa chỉ nhận lệnh? | Không nhất thiết. Nếu cùng hệ thống, có thể giữ định danh tài sản và cập nhật phân công; cách xử lý credential tùy chính sách. | Không nhất thiết đổi token. Token có thể tiếp tục dùng nếu vẫn là bản cài đó và cùng Firebase project. |
| Ai quyết định A hay B được bấm Tìm? | Cơ chế phân quyền trên máy chủ. | Cũng là cơ chế phân quyền trên máy chủ; token không chứa quyền quản lý cửa hàng. |
| Lệnh cũ của A đang chờ thì sao? | Phải kết thúc hoặc vô hiệu hóa theo quy trình bàn giao. Lệnh đã trả về PDA trước thời điểm chuyển vẫn có thể đang được xử lý. | Cũng phải vô hiệu hóa; message đã giao cho Google có thể còn đến trễ trước khi hết hạn. |
| Máy đổi Wi-Fi hoặc IP | Không cần đổi định danh chỉ vì đổi mạng; cần truy cập được API. | Không cần tự đổi token chỉ vì đổi Wi-Fi/IP; app phải đồng bộ nếu SDK thực sự cấp token mới. |
| B sử dụng máy chủ hoặc Firebase project riêng | Có thể cần đổi địa chỉ API và đăng ký lại. | Có thể cần cấu hình Firebase phù hợp, cập nhật app và đăng ký lại địa chỉ nhận lệnh. |

FCM token là địa chỉ kỹ thuật của bản cài ứng dụng, không phải mã tài sản cố định. Cần duy trì việc cập nhật token và xử lý token không còn hợp lệ. Tham khảo [quản lý token FCM](https://firebase.google.com/docs/cloud-messaging/manage-tokens).

### Quy trình bàn giao nên có

Đây là **quy trình đề xuất**, cần được phát triển và nghiệm thu nếu khách hàng yêu cầu quản lý điều chuyển đầy đủ:

1. Người có quyền tạo yêu cầu điều chuyển, ghi rõ thiết bị, cửa hàng giao, cửa hàng nhận và thời điểm áp dụng.
2. Tạm ngăn yêu cầu tìm mới trong lúc bàn giao; dừng yêu cầu đang chạy và xử lý các lệnh còn chờ. Nếu máy offline, giữ trạng thái bàn giao chưa hoàn tất hoặc chờ lệnh cũ hết hạn theo chính sách đã thống nhất.
3. Cập nhật cửa hàng quản lý và quyền thao tác trong cùng quy trình. Giữ mã tài sản, lịch sử điều chuyển và người thực hiện; thu hồi hoặc cấp lại credential nếu cần.
4. Đồng bộ thông tin xuống PDA. Với FCM, kiểm tra địa chỉ nhận lệnh còn hợp lệ; với polling, kiểm tra máy gọi API được bằng credential hiện hành.
5. Từ tài khoản của B, thử Tìm và Dừng; từ tài khoản A, xác nhận quyền đã thay đổi đúng. Chỉ hoàn tất bàn giao khi kết quả phù hợp.

Nếu cần bảo đảm một lệnh của cửa hàng cũ không thể bật chuông sau bàn giao, nên bổ sung **phiên bản phân công thiết bị** vào lệnh và đối chiếu trên PDA, hoặc kiểm tra lại quyền thực thi với máy chủ trước khi bật chuông. Cách kiểm tra lại qua mạng tăng thêm phụ thuộc kết nối; đây là lựa chọn thiết kế cần thống nhất, không phải tính năng sẵn có của FCM.

### Bản hiện tại hỗ trợ đến đâu?

Hiện tại, cửa hàng được lấy từ tài khoản lúc đăng ký PDA. Luồng đăng ký tạo bản ghi thiết bị mới; chưa có quy trình điều chuyển riêng để cập nhật phân công, giữ lịch sử và phối hợp xử lý lệnh đang chạy.

Hệ thống có chức năng xóa rồi đăng ký lại, nhưng **xóa thiết bị sẽ xóa các yêu cầu tìm, log cảnh báo và outbox liên quan**, dù audit chung vẫn được giữ. Cách này không đáp ứng đầy đủ nhu cầu giữ lịch sử điều chuyển tài sản. Chi tiết tại [xóa và đăng ký lại PDA](22-delete-device-and-finder-troubleshooting.md).

Website finder hiện tại cũng cho phép thao tác trên các cửa hàng mà không đăng nhập. Vì vậy, yêu cầu “sau bàn giao A không còn được tìm máy của B” cần bổ sung đăng nhập và phân quyền trên luồng website/API này, dù chọn polling hay FCM. Payload lệnh hiện tại chưa mang phiên bản phân công để kiểm tra tình huống bàn giao nêu trên.

## 5. Các tình huống nghiệp vụ khác

| Tình huống | Với polling | Với FCM | Điều cần thống nhất khi vận hành |
| --- | --- | --- | --- |
| Đổi ca, nhân viên đăng xuất | Luồng nhận lệnh có thể tiếp tục vì credential thiết bị độc lập với tài khoản nhân viên. | Đăng xuất nhân viên không tự hủy địa chỉ nhận lệnh của thiết bị. | Máy vẫn cần được tìm khi không ai đăng nhập hay chỉ trong ca làm việc? |
| Cho cửa hàng khác mượn máy một ngày | Cần cập nhật hoặc bổ sung quyền tạm thời trên máy chủ. | Tương tự; đổi token không giải quyết quyền mượn máy. | Ai chịu trách nhiệm, ai được tìm và quyền tạm thời hết hạn khi nào? |
| Cài cập nhật app | Cần xác nhận credential còn nguyên và service nhận lệnh hoạt động lại. | Cần xác nhận đăng ký thiết bị và đồng bộ token còn hoạt động. | Có bước kiểm tra Tìm/Dừng sau cập nhật hàng loạt. |
| Gỡ app, xóa dữ liệu, thay máy | Có thể mất credential, cần đăng ký lại và xử lý bản ghi cũ. | Cũng cần đăng ký lại; địa chỉ FCM có thể thay đổi. | Giữ quan hệ với mã tài sản, tránh hai bản ghi cùng được hiểu là một máy. |
| PDA hỏng, hết pin hoặc bị mất | Không nhận được lệnh nếu máy không hoạt động/kết nối. | Tương tự. | Chức năng phát chuông không thay thế định vị, khóa từ xa hay quy trình xử lý mất tài sản. |
| Nhân viên bấm Tìm nhiều lần | Cần chống yêu cầu trùng và điều khiển đúng phiên tìm. | Cần xử lý cả yêu cầu trùng lẫn message đến trễ. | Bản hiện tại chỉ cho một yêu cầu đang hoạt động trên mỗi PDA và dùng mã yêu cầu để chống lặp. |
| Thu hồi hoặc thanh lý PDA | Thu hồi credential và ngừng phân công thiết bị. | Cần thêm việc ngừng sử dụng địa chỉ nhận FCM của bản cài cũ. | Xóa bản ghi phía máy chủ không phải thao tác xóa app hoặc xóa dữ liệu từ xa. |
| Cửa hàng cấm ứng dụng chạy nền thường trực | Khó đáp ứng polling liên tục nếu chính sách không cho phép service hoạt động. | Không cần service polling thường trực, nhưng khi phát chuông vẫn cần cơ chế chạy âm thanh của Android. | Kiểm tra chính sách thực tế trước khi chọn giải pháp. |

## 6. So sánh kỹ thuật và công vận hành

| Hạng mục | Polling | FCM |
| --- | --- | --- |
| Thành phần nhận lệnh | API HTTP và service hỏi lệnh trên PDA. | Firebase phía máy chủ, SDK/receiver trên PDA và đồng bộ token. |
| Kết nối mạng | PDA phải tới được API. | Máy chủ và PDA cần kết nối Google phù hợp; PDA vẫn cần API để đăng ký và gửi phản hồi. |
| Nhận biết thiết bị còn hoạt động | Các lượt gọi là dữ liệu hữu ích để theo dõi, nếu có ghi nhận và quy tắc xác định. | Có token hoặc gửi push thành công chưa chứng minh máy đang online; cần phản hồi hoặc cơ chế kiểm tra riêng. |
| Xác thực | API xác thực định danh và secret của PDA; người thao tác cần được kiểm tra quyền. | Quyền gửi phía Firebase và quyền người thao tác ở máy chủ; token FCM không thay thế credential gọi API. |
| Lệnh trùng hoặc đến trễ | Lệnh có thể được trả lại qua nhiều lượt hỏi; app phải xử lý an toàn. | App vẫn phải xử lý trùng, trễ và lệnh dừng đến trước lệnh tìm. |
| Tăng quy mô | Cần dự trù tải API/database, điều chỉnh tần suất và giãn nhịp khi lỗi; có thể bổ sung độ lệch ngẫu nhiên để tránh nhiều máy hỏi cùng lúc. | Cần theo dõi hàng đợi gửi, lỗi, retry và năng lực máy chủ; dùng FCM không tự giải quyết mọi điểm nghẽn. |
| Chẩn đoán “không kêu” | Kiểm tra lượt hỏi gần nhất, credential, lệnh trả về, service và audio. | Kiểm tra hàng đợi gửi, cấu hình Firebase, token, log giao/nhận, phản hồi và audio. |
| Dữ liệu qua bên thứ ba | Luồng nhận lệnh có thể chỉ đi qua hạ tầng doanh nghiệp. | Nội dung gửi qua FCM đi qua dịch vụ Google; nên giới hạn payload ở dữ liệu điều khiển cần thiết. |

Trong bản hiện có, APK polling vẫn có SDK Firebase và có thể đồng bộ token khi được cấu hình. Vì vậy, yêu cầu “không có bất kỳ giao tiếp nào với Google” cần được đánh giá và cấu hình riêng; chỉ chọn chế độ polling chưa đủ để khẳng định điều đó.

## 7. Chi phí: nên tính theo cả vòng đời sử dụng

FCM hiện được Firebase liệt kê là dịch vụ không thu phí. Tuy nhiên, doanh nghiệp vẫn trả chi phí máy chủ ứng dụng, database, kết nối, giám sát và hỗ trợ. Xem [bảng giá Firebase](https://firebase.google.com/pricing).

Polling không cần phí dịch vụ push riêng, nhưng tạo tải ngay cả khi không có ai tìm PDA. Có thể hình dung bằng công thức:

> Số lượt hỏi mỗi ngày ≈ số PDA × số giây hoạt động mỗi ngày ÷ chu kỳ hỏi.

Ví dụ dưới đây giả định mỗi máy hoạt động liên tục 12 giờ/ngày, hỏi đúng mỗi 5 giây, bỏ qua thời gian HTTP và không gặp lỗi. Đây là phép tính minh họa, không phải kết quả đo hoặc dự báo chính xác cho bản hiện tại.

| Số PDA | Lượt hỏi trung bình mỗi giây khi tất cả đang hoạt động | Lượt hỏi trong 12 giờ |
| --- | ---: | ---: |
| 100 | 20 | 864.000 |
| 1.000 | 200 | 8.640.000 |
| 5.000 | 1.000 | 43.200.000 |

Các con số này chưa bao gồm website làm mới dữ liệu, nghiệp vụ khác, phản hồi từ PDA hay chi phí database. Nhịp hỏi thưa hơn giảm tải nhưng làm tăng thời gian chờ; chỉ polling trong giờ làm giảm tải nhưng phải giải quyết nhu cầu tìm máy ngoài giờ.

Với FCM, không có lượng request hỏi lệnh như bảng trên. Tuy vậy, bản triển khai hiện tại vẫn có các request HTTP đồng bộ và hàng đợi gửi cần được đo tải. Không thể dùng số liệu về năng lực của dịch vụ Google để thay cho kiểm thử máy chủ ứng dụng.

Khi so tổng chi phí, nên tính cùng nhau: triển khai ban đầu, máy chủ, dữ liệu di động nếu có, pin trong một ca, công cấu hình từng model, cập nhật app và thời gian đội hỗ trợ xử lý sự cố. Chưa có số đo thực tế thì không nên khẳng định một phương án rẻ hơn theo một tỷ lệ cụ thể.

## 8. Những gì đã có và những gì cần làm thêm

| Nội dung | Trạng thái trong hệ thống hiện tại |
| --- | --- |
| Hai cách nhận lệnh | Có FCM và polling; mặc định là FCM. |
| Chọn cách nhận lệnh | Chọn khi build và cài APK trên PDA đích. Chưa có nút chuyển từ xa hoặc đổi tức thời trong app. |
| FCM lỗi thì tự chuyển polling | **Chưa có.** Thiếu token, lỗi Firebase hoặc tắt FCM ở máy chủ không tự bật polling. |
| Nhịp polling | Chờ 5 giây sau lượt thành công; giãn nhịp khi lỗi, tối đa 60 giây. |
| Xử lý âm thanh | Hai phương án dùng chung xử lý lệnh, service chuông, hạn lệnh và phản hồi HTTP. |
| Đội máy dùng lẫn hai loại APK | Có thể chọn khác nhau theo PDA; máy chủ hiện chưa quản lý chế độ build của từng máy. Cần kiểm soát phiên bản được cài. |
| Điều chuyển giữa cửa hàng có lịch sử | Chưa có quy trình chuyên biệt. Cần bổ sung nếu là yêu cầu nghiệm thu. |
| Phân quyền website theo cửa hàng | Website finder hiện công khai trong phạm vi triển khai, không đăng nhập. Cần bổ sung nếu yêu cầu giới hạn người thao tác. |
| Tự hồi phục polling sau reboot | Chưa có boot receiver; cần mở Home. |
| Số liệu cam kết độ trễ, tỷ lệ thành công và pin | Tài liệu này chưa có kết quả đo trên đội PDA của khách hàng. Cần chạy thử trước khi cam kết. |

Chi tiết kỹ thuật hiện có: [polling trên PDA](15-finder-polling.md), [cấu hình chọn transport](16-fcm-to-polling-fallback.md), [luồng FCM phát chuông](20-fcm-to-pda-alarm-guide.md), [website finder](21-react-device-finder.md).

## 9. Nên chọn phương án nào?

| Điều kiện thực tế | Hướng lựa chọn | Điều kiện cần kiểm chứng |
| --- | --- | --- |
| PDA hỗ trợ Google, mạng tới FCM ổn định, dùng pin cả ca | Ưu tiên thử nghiệm FCM. | Nhận lệnh khi khóa màn hình, chạy nền và thời gian gửi phản hồi. |
| PDA không có môi trường Google phù hợp | Ưu tiên thử nghiệm polling. | Khả năng duy trì service, mạng nền và mức pin trên đúng thiết bị. |
| Chỉ cho phép kết nối mạng riêng, không cho phép dịch vụ push Google | Ưu tiên polling. | API thực sự truy cập được từ mọi cửa hàng; rà soát cấu hình SDK nếu cấm toàn bộ kết nối Google. |
| Có nhiều nghìn PDA nhưng ít khi dùng chức năng tìm | Nghiêng về FCM nếu hạ tầng cho phép. | Kiểm thử hàng đợi, đợt gửi đồng thời và tải phản hồi. |
| Có nhiều loại PDA với khả năng khác nhau | Có thể phân nhóm sử dụng FCM/polling. | Có danh mục cấu hình, quản lý APK và hướng dẫn hỗ trợ theo nhóm. |
| Thường xuyên đổi cửa hàng hoặc cho mượn máy | Chọn kênh theo thiết bị/mạng; phát triển thêm nghiệp vụ điều chuyển. | Lịch sử, phân quyền, xử lý lệnh cũ và kiểm tra sau bàn giao. |
| Muốn tự chuyển khi kênh chính gặp lỗi | Cân nhắc phát triển mô hình kết hợp. | Xác định thế nào là lỗi, khi nào bật/tắt dự phòng, chống trùng và mức tải/pin chấp nhận được. |

Mô hình kết hợp FCM và polling là một hướng mở rộng, chưa phải chế độ có sẵn. Nó có thể giúp trong một số lỗi riêng của kênh FCM, nhưng không khắc phục được việc máy hết pin hoặc mất mọi kết nối. Cũng cần tính công vận hành hai kênh và mức tiêu thụ nền khi kênh dự phòng hoạt động.

## 10. Chạy thử thế nào để quyết định có cơ sở?

Nên thử trên các model PDA và mạng cửa hàng đại diện, trong trọn một ca làm việc, với cùng tập tình huống cho cả hai phương án. Khách hàng và đội triển khai thống nhất ngưỡng chấp nhận trước khi đo.

| Nội dung cần đo | Cách đánh giá |
| --- | --- |
| Thời gian bật chuông | Đo từ lúc bấm Tìm đến khi thực sự nghe tiếng chuông; theo dõi riêng thời điểm website cập nhật. |
| Những lần chậm | Ngoài trung bình, xem mức thời gian mà 95% lần thử hoàn tất trong khoảng đó và các lần vượt ngưỡng. |
| Tỷ lệ thành công | Số lần máy kêu trong thời hạn chia cho tổng số lần thử; công bố rõ trạng thái mạng và thiết bị. |
| Thời gian dừng | Thử dừng từ website và tại PDA, kể cả khi mất mạng giữa lúc đang reo. |
| Pin và độ ổn định trong ca | So sánh cùng model, thời lượng, tác vụ và điều kiện mạng; ghi nhận pin cuối ca và service bị dừng. |
| Các trạng thái khó | Khóa màn hình lâu, tiết kiệm pin, chuyển Wi-Fi, mất mạng/có lại, khởi động lại, đóng app và buộc dừng. |
| Tải đồng thời | Nhiều PDA hoạt động và nhiều yêu cầu tìm cùng lúc; theo dõi hàng đợi, API và database. |
| Vòng đời thiết bị | Đổi ca, cập nhật app, đăng ký lại, chuyển A → B và thử quyền của cả hai cửa hàng. |
| Lệnh cũ | Tìm rồi Dừng nhanh, nhận lệnh trễ, hết hạn, bàn giao trong lúc còn lệnh; không phát chuông sai phiên. |
| Khả năng hỗ trợ | Đội vận hành có xác định được lỗi ở máy chủ, mạng, kênh nhận hay âm thanh từ dữ liệu ghi nhận không? |

Nếu đội PDA đáp ứng điều kiện Google và không có ràng buộc mạng riêng, có thể bắt đầu bằng FCM để đánh giá khả năng phản hồi và mức pin. Nếu điều kiện thiết bị hoặc mạng không phù hợp, polling là lựa chọn thực tế cần thử trên cấu hình chạy nền được quản lý. Kết quả chạy thử, cùng yêu cầu bàn giao và phân quyền, sẽ là cơ sở chốt phương án triển khai.
