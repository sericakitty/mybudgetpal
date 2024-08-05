import AppContent from './components/TheAppContent';
import { AuthProvider, useAuth } from './components/TheAuthContext';
import { NotificationProvider } from './components/TheNotificationContext';
import TheNotification from './components/TheNotification';
import { BrowserRouter as Router, Navigate } from 'react-router-dom';

import './App.css';

const LoadingScreen = () => (
  <div className="loading-screen">
    Loading...
  </div>
);

const AppWrapper = () => {
  const { loading, error } = useAuth();

  if (loading) {
    return <LoadingScreen />;
  }

  if (error) {
    return <Navigate to="/login" />;
  }

  return <AppContent />;
};

const App = () => (
  <Router>
    <NotificationProvider>
      <AuthProvider>
        <TheNotification />
        <AppWrapper />
      </AuthProvider>
    </NotificationProvider>
  </Router>
);

export default App;
