import { createContext, useState, useContext } from 'react';
import PropTypes from 'prop-types';

const TheNotificationContext = createContext();

export const useNotification = () => useContext(TheNotificationContext);

export const NotificationProvider = ({ children }) => {
  const [notification, setNotification] = useState({ message: '', type: '' });

  const showNotification = (message, type) => {
    setNotification({ message, type });
    setTimeout(() => {
      setNotification({ message: '', type: '' });
    }, 3000);
  };

  return (
    <TheNotificationContext.Provider value={{ notification, showNotification }}>
      {children}
    </TheNotificationContext.Provider>
  );
};

NotificationProvider.propTypes = {
  children: PropTypes.node.isRequired,
};

export default TheNotificationContext;
