import React, { Component } from 'react';
import CssBaseline from '@material-ui/core/CssBaseline';
import './App.css';
import MenuAppBar from "./MenuAppBar";

class App extends Component {
    render() {
        return (
            <React.Fragment>
                <CssBaseline />
                <MenuAppBar />
            </React.Fragment>
    );
    }
}

export default App;