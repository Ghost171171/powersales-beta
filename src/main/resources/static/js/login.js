function updateProfileUI() {
    if (APP_STATE.currentUser) {
        renderProfile(APP_STATE.currentUser);
    } else {
        showLoginScreen();
    }
}

function showLoginScreen() {
    const sidebarProfile = document.getElementById('profile-content');

    sidebarProfile.innerHTML =
        `       <form id="loginForm">
                <h2>Login</h2>
                <input id="username" placeholder="Username" />
                <input id="password" type="password" placeholder="Password" />
                <button type="submit">Login</button>
                <p id="loginError" style="color:red; display:none;"></p>
                </form>`;

    document.getElementById("loginForm").addEventListener('submit', async e => {
        e.preventDefault();

        const username = document.getElementById("username").value.trim();
        const password = document.getElementById("password").value;
        await handleLogin(username, password);
    });
}