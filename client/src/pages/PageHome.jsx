import { useState, useEffect } from 'react';
import axiosInstance from '../services/';
import { makeStyles } from '@material-ui/core';
import { useAuth } from '../components/TheAuthContext';

const useStyles = makeStyles({
  centerContainer: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    height: '75vh',
  },
  containerBox: {
    background: '#fff',
    padding: '30px',
    boxShadow: '0 0 10px rgba(0, 0, 0, 0.1)',
    borderRadius: '10px',
    width: '100%',
    maxWidth: '400px',
  },
  dropZone: {
    border: '2px dashed #ccc',
    borderRadius: '5px',
    padding: '20px',
    textAlign: 'center',
    color: '#ccc',
    margin: '10px 0',
    position: 'relative',
    cursor: 'pointer',
  },
  dropZoneDragover: {
    borderColor: '#000',
    color: '#000',
    backgroundColor: '#f1f1f1',
  },
  fileList: {
    listStyle: 'none',
    padding: 0,
  },
  fileListLi: {
    color: '#28a745',
    marginTop: '5px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: '5px',
    border: '1px solid #ddd',
    borderRadius: '3px',
  },
  fileListLiButton: {
    marginLeft: '10px',
  },
  savedText: {
    marginLeft: 'auto',
  },
  containerBoxH1: {
    textAlign: 'center',
    marginBottom: '20px',
  },
  containerBoxH2: {
    textAlign: 'center',
    marginBottom: '20px',
  },
  containerBoxH3: {
    textAlign: 'center',
    marginBottom: '20px',
  },
  formGroup: {
    marginBottom: '15px',
  },
  formGroupLabel: {
    display: 'block',
    marginBottom: '5px',
  },
  btnBlock: {
    display: 'block',
    width: '100%',
  },
  mt3: {
    marginTop: '15px',
  },
  linkContainer: {
    display: 'block',
    marginTop: '10px',
    textAlign: 'center',
  },
  alert: {
    marginBottom: '20px',
    marginTop: '10px',
  },
  container: {
    marginTop: '20px',
  },
  enableText: {
    color: '#ff9800',
    fontWeight: 'bold',
    marginTop: '20px',
    textAlign: 'center',
  },
});

const Home = () => {
  const classes = useStyles();
  const { user } = useAuth();
  const [messages, setMessages] = useState([]);
  const [errors, setErrors] = useState([]);
  const [validFiles, setValidFiles] = useState(new DataTransfer());

  useEffect(() => {
    const timeout = setTimeout(() => {
      setMessages([]);
    }, 5000);

    return () => clearTimeout(timeout);
  }, [messages]);

  const handleFiles = (files) => {
    const newFiles = Array.from(files).filter(file =>
      file.name.toLowerCase().endsWith('.csv') || file.type === 'text/csv'
    );

    newFiles.forEach(file => {
      if (!fileListContains(file.name)) {
        appendFileToList(file);
        validFiles.items.add(file);
      } else {
        displayError(`${file.name} is already added.`);
      }
    });

    setValidFiles(validFiles);
    document.getElementById('file').files = validFiles.files;
  };

  const fileListContains = (fileName) => {
    return Array.from(validFiles.files).some(file => file.name === fileName);
  };

  const appendFileToList = (file) => {
    const fileList = document.getElementById('file-list');
    const li = document.createElement('li');
    li.textContent = file.name;

    const savedText = document.createElement('span');
    savedText.className = 'saved-text';
    savedText.textContent = 'Saved';
    savedText.style.color = '#28a745';
    li.appendChild(savedText);

    const removeBtn = document.createElement('button');
    removeBtn.textContent = 'Remove';
    removeBtn.className = 'btn btn-danger btn-sm';
    removeBtn.onclick = () => {
      li.remove();
      removeFileFromList(file);
    };

    li.appendChild(removeBtn);
    fileList.appendChild(li);
  };

  const removeFileFromList = (fileToRemove) => {
    const newValidFiles = new DataTransfer();
    Array.from(validFiles.files).forEach(file => {
      if (file.name !== fileToRemove.name) {
        newValidFiles.items.add(file);
      }
    });
    setValidFiles(newValidFiles);
    document.getElementById('file').files = newValidFiles.files;
  };

  const displayError = (message) => {
    setErrors([...errors, message]);
    setTimeout(() => {
      setErrors(errors.filter(error => error !== message));
    }, 5000);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const formData = new FormData();
    Array.from(validFiles.files).forEach(file => formData.append('file', file));

    try {
      const response = await axiosInstance.post('/api/entries/import-data', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      const { uploadedFiles, failedFiles } = response.data;

      if (uploadedFiles.length > 0) {
        uploadedFiles.forEach(file => {
          setMessages(messages => [...messages, `${file} uploaded successfully.`]);
        });
        // Clear the file list
        setValidFiles(new DataTransfer());
        document.getElementById('file-list').innerHTML = '';
        document.getElementById('file').value = null;
      }

      if (failedFiles.length > 0) {
        failedFiles.forEach(file => displayError(`${file} failed to upload.`));
      }
    } catch (error) {
      displayError('An error occurred while uploading the files.');
    }
  };

  useEffect(() => {
    const dropZone = document.getElementById('drop-zone');
    const fileInput = document.getElementById('file');

    const preventDefaults = (e) => {
      e.preventDefault();
      e.stopPropagation();
    };

    const highlight = () => dropZone.classList.add('dragover');
    const unhighlight = () => dropZone.classList.remove('dragover');

    const handleDrop = (e) => {
      handleFiles(e.dataTransfer.files);
      unhighlight();
    };

    dropZone.addEventListener('click', () => fileInput.click());
    fileInput.addEventListener('change', (event) => handleFiles(event.target.files));

    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
      dropZone.addEventListener(eventName, preventDefaults, false);
    });

    ['dragenter', 'dragover'].forEach(eventName => {
      dropZone.addEventListener(eventName, highlight, false);
    });

    ['dragleave', 'drop'].forEach(eventName => {
      dropZone.addEventListener(eventName, unhighlight, false);
    });

    dropZone.addEventListener('drop', handleDrop);

    return () => {
      dropZone.removeEventListener('click', () => fileInput.click());
      fileInput.removeEventListener('change', (event) => handleFiles(event.target.files));

      ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        dropZone.removeEventListener(eventName, preventDefaults, false);
      });

      ['dragenter', 'dragover'].forEach(eventName => {
        dropZone.removeEventListener(eventName, highlight, false);
      });

      ['dragleave', 'drop'].forEach(eventName => {
        dropZone.removeEventListener(eventName, unhighlight, false);
      });

      dropZone.removeEventListener('drop', handleDrop);
    };
  }, [validFiles]);

  return (
    <>
      {!user?.enabled ? (
        <div className={classes.centerContainer}>
          <div className={classes.containerBox}>
            <div className={classes.enableText}>
              Please verify your email to access all features.
            </div>
          </div>
        </div>
      ) : (
        <div className={classes.centerContainer}>
          <div className={classes.containerBox}>
            <h1 className={classes.containerBoxH1}>My Budget Pal</h1>
            <div>
              <p>CSV file is only supported</p>
              <form onSubmit={handleSubmit} encType="multipart/form-data">
                <div className={classes.dropZone} id="drop-zone">
                  <p>Drag and drop files here or click to select files</p>
                  <input type="file" id="file" name="file" multiple required style={{ display: 'none' }} />
                </div>
                <ul id="file-list" className={classes.fileList}></ul>
                <button type="submit" className="btn btn-primary">Import</button>
              </form>
            </div>
            {messages.map((message, index) => (
              <div key={index} className="alert alert-success">{message}</div>
            ))}
            {errors.map((error, index) => (
              <div key={index} className="alert alert-danger">{error}</div>
            ))}
          </div>
        </div>
      )}
    </>
  );
};

export default Home;
