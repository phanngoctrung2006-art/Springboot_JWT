$(document).ready(function() {
    // Hiển thị thông tin người dùng đăng nhập thành công
    $.ajax({
        type: 'GET',
        url: '/users/me',
        dataType: 'json',
        contentType: "application/json; charset=utf-8",
        beforeSend: function(xhr) {
            if (localStorage.token) {
                xhr.setRequestHeader('Authorization', 'Bearer ' + localStorage.token);
            }
        },
        success: function(data) {
            var json = JSON.stringify(data, null, 4);
            $('#profile').html(data.fullName);
            if (data.images) {
                var imgElem = document.getElementById("images");
                if (imgElem) {
                    imgElem.src = data.images;
                    imgElem.style.display = "inline-block";
                }
            }
        },
        error: function(e) {
            var json = e.responseText;
            $('#feedback').html("Bạn chưa đăng nhập hoặc phiên làm việc đã hết hạn.");
            alert("Sorry, you are not logged in.");
        }
    });

    // Hàm đăng xuất
    $('#logout').click(function() {
        localStorage.clear();
        window.location.href = "/login";
    });

    // Hàm login
    function doLogin() {
        var email = document.getElementById('email').value;
        var password = document.getElementById('password').value;

        if (!email || !password) {
            alert("Vui lòng nhập đầy đủ Email và Mật khẩu!");
            return;
        }

        var basicInfo = JSON.stringify({
            email: email,
            password: password
        });

        $.ajax({
            type: "POST",
            url: "/auth/login",
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            data: basicInfo,
            success: function(data) {
                localStorage.token = data.token;
                window.location.href = "/user/profile";
            },
            error: function() {
                alert("Login Failed - Vui lòng kiểm tra lại email hoặc mật khẩu!");
            }
        });
    }

    $('#login').click(doLogin);

    // Hỗ trợ nhấn Enter khi đang nhập mật khẩu
    $('#password, #email').keypress(function(e) {
        if (e.which === 13) {
            doLogin();
        }
    });
});
