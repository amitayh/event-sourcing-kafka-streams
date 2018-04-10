import React, {PureComponent} from 'react';

export default class Message extends PureComponent {
  render() {
    const {message} = this.props;
    return (message !== null) ? <h2>{message}</h2> : null;
  }
}
