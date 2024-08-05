import { Route, Routes, useLocation } from 'react-router-dom';
import PageSignup from '../pages/PageSignup';
import PageVerifyEmail from '../pages/PageVerifyEmail';
import PageForgotPassword from '../pages/PageForgotPassword';
import PageResetPassword from '../pages/PageResetPassword';
import PageEntryList from '../pages/PageEntryList';
import PageStatisticList from '../pages/PageStatisticList';
import PageKeywordsList from '../pages/PageKeywordsList';
import PageEditKeywords from '../pages/PageEditKeywords';
import PageAddKeywords from '../pages/PageAddKeywords';
import TheNavbar from './TheNavbar';
import PageLogin from '../pages/PageLogin';
import UserProfile from '../pages/PageUserProfile';
import PageHome from '../pages/PageHome';
import TheAuthenticatedRoute from './TheAuthenticatedRoute';
import { useAuth } from './TheAuthContext';

const AppContent = () => {
  const { user, logout } = useAuth();
  const location = useLocation();

  // Define paths where the navbar should not be displayed
  const noNavbarPaths = [
    '/signup',
    '/verify-email',
    '/forgot-password',
    '/reset-password',
    '/login',
    '/404',
  ];

  // Check if the current path is in the list of no-navbar paths
  const shouldShowNavbar = !noNavbarPaths.includes(location.pathname);

  return (
    <>
      {user && shouldShowNavbar && <TheNavbar logout={logout} user={user} />}
      <div style={{ marginTop: user && shouldShowNavbar ? '56px' : '0' }}>
        <Routes>
          <Route path="/signup" element={<PageSignup />} />
          <Route path="/verify-email" element={<PageVerifyEmail />} />
          <Route path="/forgot-password" element={<PageForgotPassword />} />
          <Route path="/reset-password" element={<PageResetPassword />} />
          <Route path="/login" element={<PageLogin />} />
          <Route path="/" element={<TheAuthenticatedRoute component={PageHome} />} />
          <Route path="/entries" element={<TheAuthenticatedRoute component={PageEntryList} />} />
          <Route path="/profile" element={<TheAuthenticatedRoute component={UserProfile} />} />
          <Route path="/keywords" element={<TheAuthenticatedRoute component={PageKeywordsList} />} />
          <Route path="/keywords/edit/:id" element={<TheAuthenticatedRoute component={props => <PageEditKeywords {...props} type="INCLUDED" />} />} />
          <Route path="/keywords/edit/excluded/:id" element={<TheAuthenticatedRoute component={props => <PageEditKeywords {...props} type="EXCLUDED" />} />} />
          <Route path="/keywords/new" element={<TheAuthenticatedRoute component={props => <PageAddKeywords {...props} type="INCLUDED" />} />} />
          <Route path="/keywords/new/excluded" element={<TheAuthenticatedRoute component={props => <PageAddKeywords {...props} type="EXCLUDED" />} />} />
          <Route path="/statistics" element={<TheAuthenticatedRoute component={PageStatisticList} />} />
          <Route path="*" element={<div>404 Page Not Found</div>} />
        </Routes>
      </div>
    </>
  );
};

export default AppContent;
