/**
 * Profile page — displays account details and provides password change.
 */
const ProfilePage = (() => {
    async function render(container) {
        container.innerHTML = `
            <div class="page-header">
                <h1>My Profile</h1>
                <p class="subtitle">View your account details and change your password</p>
            </div>
            <div class="page-body">
                <div id="profile-content">
                    <div class="loading-center"><div class="spinner"></div></div>
                </div>
            </div>
        `;

        try {
            const profile = await API.get('/profile');
            renderProfile(profile);
        } catch (e) {
            Toast.error('Failed to load profile: ' + e.message);
        }
    }

    function renderProfile(p) {
        const target = document.getElementById('profile-content');
        const isStudent = p.role === 'STUDENT';

        let fields = `
            <div class="profile-field">
                <div class="profile-field-label">Full Name</div>
                <div class="profile-field-value">${esc(p.firstName || '')} ${esc(p.lastName || '')}</div>
            </div>
            <div class="profile-field">
                <div class="profile-field-label">Username</div>
                <div class="profile-field-value">${esc(p.username)}</div>
            </div>
            <div class="profile-field">
                <div class="profile-field-label">Role</div>
                <div class="profile-field-value">${esc(p.role)}</div>
            </div>
            <div class="profile-field">
                <div class="profile-field-label">Email</div>
                <div class="profile-field-value">${esc(p.email || '-')}</div>
            </div>
            <div class="profile-field">
                <div class="profile-field-label">Phone</div>
                <div class="profile-field-value">${esc(p.phone || '-')}</div>
            </div>
        `;

        if (isStudent) {
            fields += `
                <div class="profile-field">
                    <div class="profile-field-label">Registration Number</div>
                    <div class="profile-field-value">${esc(p.registrationNumber || '-')}</div>
                </div>
                <div class="profile-field">
                    <div class="profile-field-label">Department</div>
                    <div class="profile-field-value">${esc(p.department || '-')}</div>
                </div>
                <div class="profile-field">
                    <div class="profile-field-label">Course</div>
                    <div class="profile-field-value">${esc(p.course || '-')}</div>
                </div>
                <div class="profile-field">
                    <div class="profile-field-label">Semester</div>
                    <div class="profile-field-value">${p.semester || '-'}</div>
                </div>
                <div class="profile-field">
                    <div class="profile-field-label">Membership Status</div>
                    <div class="profile-field-value">${esc(p.membershipStatus || '-')}</div>
                </div>
                <div class="profile-field">
                    <div class="profile-field-label">Borrow Limit</div>
                    <div class="profile-field-value">${p.currentBorrowCount || 0} / ${p.borrowLimit || 0}</div>
                </div>
                <div class="profile-field">
                    <div class="profile-field-label">Fine Balance</div>
                    <div class="profile-field-value">₹${(p.fineBalance || 0).toFixed(2)}</div>
                </div>
            `;
        }

        target.innerHTML = `
            <div class="profile-grid mb-md">${fields}</div>
            <button class="btn btn-primary" id="change-pwd-btn">Change Password</button>
        `;

        document.getElementById('change-pwd-btn').addEventListener('click', showChangePasswordDialog);
    }

    function showChangePasswordDialog() {
        const form = document.createElement('div');
        form.innerHTML = `
            <div class="form-group">
                <label class="form-label">Current Password</label>
                <input class="form-input" id="pwd-old" type="password" placeholder="Enter current password">
            </div>
            <div class="form-group">
                <label class="form-label">New Password</label>
                <input class="form-input" id="pwd-new" type="password" placeholder="Min. 8 characters">
            </div>
            <div class="form-group">
                <label class="form-label">Confirm New Password</label>
                <input class="form-input" id="pwd-confirm" type="password" placeholder="Re-enter new password">
            </div>
            <p class="text-muted text-sm mt-sm">
                Password must be at least 8 characters with at least one letter and one digit.
            </p>
        `;

        Modal.open({
            title: 'Change Password',
            content: form,
            actions: [
                { label: 'Cancel', onClick: () => Modal.close() },
                { label: 'Update Password', cls: 'btn-primary', onClick: async () => {
                    const oldPwd = document.getElementById('pwd-old').value;
                    const newPwd = document.getElementById('pwd-new').value;
                    const confirm = document.getElementById('pwd-confirm').value;

                    if (!oldPwd || !newPwd) {
                        Toast.error('Please fill in all password fields');
                        return;
                    }
                    if (newPwd !== confirm) {
                        Toast.error('New password and confirmation do not match');
                        return;
                    }
                    if (newPwd.length < 8) {
                        Toast.error('Password must be at least 8 characters');
                        return;
                    }

                    try {
                        await Auth.changePassword(oldPwd, newPwd);
                        Toast.success('Password changed successfully');
                        Modal.close();
                    } catch (e) {
                        Toast.error(e.message);
                    }
                }}
            ]
        });
    }

    function esc(s) { return (s || '').replace(/</g, '&lt;').replace(/>/g, '&gt;'); }

    return { render };
})();
