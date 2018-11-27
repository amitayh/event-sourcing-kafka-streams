import React, {PureComponent} from 'react';

export default class Message extends PureComponent {
  render() {
    const {message} = this.props;
    return (message !== null) ?
      <div className="alert alert-primary">{message}</div> :
      null;
  }
}
