# opencog-neo4j
Google Summer of Code 2015 Proposal to implement Neo4j Graph Backing Store as described in http://wiki.opencog.org/w/Neo4j_Backing_Store

See also:

* @cosmoharrigan's https://github.com/cosmoharrigan/opencog-neo4j
* http://lumen.hendyirawan.com/2014/07/neo4j-as-graph-database-for-opencog.html
* http://wiki.opencog.org/w/Scaling_OpenCog

## Jekyll - Local Setup

    git clone https://github.com/lumenitb/lumen-sdk.git
    cd lumen-sdk
    git checkout gh-pages

Install Ruby first, and `zlib1g-dev`, `libxml2-dev` (required by `nokogiri` gem).

    sudo aptitude install ruby-dev zlib1g-dev libxml2-dev
    sudo gem install -V bundler # for Windows, omit 'sudo'
    bundle install -V

Launch Jekyll development server by running:

    bundle exec jekyll serve --baseurl ''
