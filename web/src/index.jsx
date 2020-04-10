import React from 'react';
import ReactDOM from 'react-dom';
import 'typeface-roboto';
import App from './App';
import registerServiceWorker from './registerServiceWorker';
import './index.css';

ReactDOM.render(<App />, document.getElementById('root'));
registerServiceWorker();
