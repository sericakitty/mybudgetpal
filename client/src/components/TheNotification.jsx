import { useNotification } from './TheNotificationContext';
import { makeStyles } from '@material-ui/core';
import { useEffect, useState } from 'react';

const useStyles = makeStyles({
  notification: {
    position: 'fixed',
    top: '10px',
    right: '10px',
    padding: '10px',
    borderRadius: '5px',
    zIndex: 1000,
    transition: 'opacity 0.3s ease-in-out',
    opacity: 1,
  },
  success: {
    backgroundColor: '#4caf50',
    color: 'white',
  },
  error: {
    backgroundColor: '#f44336',
    color: 'white',
  },
  hidden: {
    opacity: 0,
  },
});

const TheNotification = () => {
  const classes = useStyles();
  const { notification } = useNotification();
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    if (notification.message) {
      setIsVisible(true);
      const timer = setTimeout(() => {
        setIsVisible(false);
      }, 3000);
      return () => clearTimeout(timer);
    }
  }, [notification]);

  if (!notification.message) return null;

  return (
    <div className={`${classes.notification} ${classes[notification.type]} ${isVisible ? '' : classes.hidden}`}>
      {notification.message}
    </div>
  );
};

export default TheNotification;
