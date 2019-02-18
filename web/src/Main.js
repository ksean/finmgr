import React, {Component} from 'react';

const API_URL = 'http://localhost:8080/';

class Main extends Component {
  constructor(props) {
    super(props);

    this.state = {
      message: '',
      isLoading: false,
      error: {},
    };
  }

  componentDidMount() {
    this.setState({ isLoading: true });

    fetch(API_URL)
      .then(response => response.json())
      .then(data => this.setState({ message: data.message, error: data.error, isLoading: false }));
  }

  render() {
    const { message, isLoading, error } = this.state;

    if (isLoading) {
      return <p>Loading ...</p>;
    }

    if (error) {
      return <p>Error retrieving welcome message</p>;
    }

    return (
        <p>{message}</p>
    );
  }

}

export default Main;
