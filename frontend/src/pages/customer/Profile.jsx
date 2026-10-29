import { useEffect, useState } from "react";
import API from "../../api/api";
import "./profile.css";

export default function Profile() {
  const [profile, setProfile] = useState(null);
  const userId = localStorage.getItem("userid");
  const accountId = localStorage.getItem("accountid");

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const customerResponse = await API.get(`/api/customers/by-user/${userId}`);
        const accountResponse = await API.get(`/accounts/${accountId}`);
        const customer = customerResponse.data;
        const account = accountResponse.data;
        setProfile({
          name: customer.fullname || localStorage.getItem("username"),
          accountNumber: account.accountnumber,
          email: customer.email,
          phone: customer.phone,
          kycStatus: customer.kycstatus || customer.kycStatus,
          balance: account.balance,
          accountType: account.accounttype,
        });
      } catch (error) {
        console.error("Profile fetch error:", error);
      }
    };
    fetchProfile();
  }, [accountId, userId]);

  if (!profile) {
    return (
      <div className="customer-page profile-page">
        <div className="profile-card"><h2>Loading Profile...</h2></div>
      </div>
    );
  }

  const fields = [
    ["Name", profile.name],
    ["Account Number", profile.accountNumber],
    ["Account Type", profile.accountType],
    ["Email", profile.email],
    ["Phone", profile.phone],
    ["KYC Status", profile.kycStatus],
    ["Balance", profile.balance],
  ];

  return (
    <div className="customer-page profile-page">
      <div className="profile-card">

    <div className="profile-header">

        <div className="profile-avatar">
            {profile.name?.charAt(0)}
        </div>

        <div>

            <div className="profile-name">
                {profile.name}
            </div>

            <div className="profile-subtitle">
                {profile.accountType} Account
            </div>

        </div>

    </div>

    <div className="profile-body">

        <div className="profile-grid">

            <div className="info-card">
                <div className="info-label">
                    Account Number
                </div>

                <div className="info-value">
                    {profile.accountNumber}
                </div>
            </div>

            <div className="info-card">
                <div className="info-label">
                    Email
                </div>

                <div className="info-value">
                    {profile.email}
                </div>
            </div>

            <div className="info-card">
                <div className="info-label">
                    Phone
                </div>

                <div className="info-value">
                    {profile.phone}
                </div>
            </div>

            <div className="info-card">
                <div className="info-label">
                    KYC Status
                </div>

                <div className="kyc-badge">
                    ✅ {profile.kycStatus}
                </div>
            </div>

            <div className="info-card">
                <div className="info-label">
                    Current Balance
                </div>

                <div className="info-value balance-value">
                    ₹ {Number(profile.balance)
                        .toLocaleString()}
                </div>
            </div>

        </div>

    </div>

</div>
    </div>
  );
}
